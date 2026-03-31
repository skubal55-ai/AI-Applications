import json
import os
import subprocess
import sys
import tempfile
import threading
import unittest
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "agents" / "angular_to_react_agent.py"


class OpenCodeHandler(BaseHTTPRequestHandler):
    created_session_payloads = []
    message_payloads = []

    def _read_json(self):
        length = int(self.headers.get("Content-Length", "0"))
        body = self.rfile.read(length).decode("utf-8")
        return json.loads(body or "{}")

    def do_GET(self):
        if self.path == "/global/health":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"healthy": True, "version": "test"}).encode("utf-8"))
            return

        self.send_response(404)
        self.end_headers()

    def do_POST(self):
        if self.path == "/session":
            payload = self._read_json()
            self.__class__.created_session_payloads.append(payload)
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"id": "session-123"}).encode("utf-8"))
            return

        if self.path == "/session/session-123/message":
            payload = self._read_json()
            self.__class__.message_payloads.append(payload)
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            response = {
                "info": {"id": "message-456"},
                "parts": [
                    {
                        "type": "text",
                        "text": "Converted summary\n```tsx\nexport function Demo() { return <div>Hello</div>; }\n```",
                    }
                ],
            }
            self.wfile.write(json.dumps(response).encode("utf-8"))
            return

        self.send_response(404)
        self.end_headers()

    def log_message(self, format, *args):
        return


def run_server():
    OpenCodeHandler.created_session_payloads = []
    OpenCodeHandler.message_payloads = []
    server = ThreadingHTTPServer(("127.0.0.1", 0), OpenCodeHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    return server, thread


class AngularToReactAgentTest(unittest.TestCase):
    def test_agent_posts_prompt_and_returns_code(self):
        server, _ = run_server()
        base_url = f"http://127.0.0.1:{server.server_port}"

        try:
            with tempfile.NamedTemporaryFile("w", suffix=".ts", delete=False) as source:
                source.write(
                    "@Component({selector: 'app-demo', template: '<button (click)=\"save()\">Save</button>'})\n"
                    "export class DemoComponent { save() {} }\n"
                )
                source_path = source.name

            result = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT),
                    "--base-url",
                    base_url,
                    "--source-file",
                    source_path,
                    "--print-prompt",
                ],
                cwd=str(ROOT),
                capture_output=True,
                text=True,
                check=True,
            )

            self.assertIn("export function Demo()", result.stdout)
            self.assertIn("Angular to React Migration Rules", result.stderr)
            self.assertTrue(OpenCodeHandler.created_session_payloads)
            self.assertTrue(OpenCodeHandler.message_payloads)

            session_payload = OpenCodeHandler.created_session_payloads[-1]
            self.assertEqual(session_payload["title"], "Angular to React Migration")

            message_payload = OpenCodeHandler.message_payloads[-1]
            self.assertEqual(message_payload["parts"][0]["type"], "text")
            self.assertIn("### Source:", message_payload["parts"][0]["text"])
            self.assertIn("React", message_payload["parts"][0]["text"])
        finally:
            server.shutdown()
            server.server_close()
            if "source_path" in locals() and os.path.exists(source_path):
                os.unlink(source_path)


if __name__ == "__main__":
    unittest.main()
