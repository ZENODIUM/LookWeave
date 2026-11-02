import { server } from "../backend/server.mjs";

export const config = { maxDuration: 300 };

export default function handler(req, res) {
  server.emit("request", req, res);
}
