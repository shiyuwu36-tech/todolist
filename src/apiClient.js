const BASE_URL = import.meta.env.VITE_API_BASE || "http://localhost:8080";

async function handleResponse(res) {
  if (res.ok) {
    const contentType = res.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      return res.json();
    }
    return res;
  }
  let errorBody;
  try {
    errorBody = await res.json();
  } catch (e) {
    errorBody = { message: res.statusText };
  }
  const err = new Error(errorBody.message || "请求失败");
  err.code = errorBody.code || "HTTP_" + res.status;
  err.details = errorBody.details;
  throw err;
}

export async function getTasks() {
  const res = await fetch(`${BASE_URL}/tasks`);
  return handleResponse(res);
}

export async function createTask(payload) {
  const res = await fetch(`${BASE_URL}/tasks`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function updateTask(id, payload) {
  const res = await fetch(`${BASE_URL}/tasks/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function deleteTask(id) {
  const res = await fetch(`${BASE_URL}/tasks/${id}`, { method: "DELETE" });
  if (!res.ok) {
    return handleResponse(res);
  }
}

export async function listAttachments(taskId) {
  const res = await fetch(`${BASE_URL}/tasks/${taskId}/attachments`);
  return handleResponse(res);
}

export async function uploadAttachment(taskId, file) {
  const form = new FormData();
  form.append("file", file);
  const res = await fetch(`${BASE_URL}/tasks/${taskId}/attachments`, {
    method: "POST",
    body: form,
  });
  return handleResponse(res);
}

export async function downloadAttachment(id, filename) {
  const res = await fetch(`${BASE_URL}/attachments/${id}`);
  if (!res.ok) {
    return handleResponse(res);
  }
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename || "download";
  link.click();
  URL.revokeObjectURL(url);
}
