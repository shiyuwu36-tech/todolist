<script setup>
import { onMounted, reactive, ref } from "vue";
import {
  createTask,
  deleteTask,
  downloadAttachment,
  getTasks,
  listAttachments,
  updateTask,
  uploadAttachment,
} from "./apiClient";
import { createDiscreteApi } from "naive-ui";

const { message } = createDiscreteApi(["message"]);

const loading = ref(false);
const errorMsg = ref("");
const tasks = ref([]);

const attachments = reactive({});

const formVisible = ref(false);
const saving = ref(false);
const editingId = ref(null);
const form = reactive({
  title: "",
  description: "",
  status: "todo",
  dueDate: "",
});

const statusOptions = [
  { label: "待办", value: "todo" },
  { label: "进行中", value: "doing" },
  { label: "完成", value: "done" },
];

const allowedTypes = ["application/pdf", "text/plain"];

const resetForm = () => {
  editingId.value = null;
  form.title = "";
  form.description = "";
  form.status = "todo";
  form.dueDate = "";
};

const openCreate = () => {
  resetForm();
  formVisible.value = true;
};

const openEdit = (task) => {
  editingId.value = task.id;
  form.title = task.title;
  form.description = task.description || "";
  form.status = task.status;
  form.dueDate = task.dueDate || "";
  formVisible.value = true;
};

const loadTasks = async () => {
  loading.value = true;
  errorMsg.value = "";
  try {
    const res = await getTasks();
    tasks.value = res.items || [];
  } catch (e) {
    errorMsg.value = e.message || "加载失败";
  } finally {
    loading.value = false;
  }
};

const saveTask = async () => {
  if (!form.title.trim()) {
    message.error("标题必填");
    return;
  }
  saving.value = true;
  try {
    const payload = {
      title: form.title,
      description: form.description,
      status: form.status,
      dueDate: form.dueDate || null,
    };
    if (editingId.value) {
      await updateTask(editingId.value, payload);
      message.success("更新成功");
    } else {
      await createTask(payload);
      message.success("创建成功");
    }
    formVisible.value = false;
    resetForm();
    await loadTasks();
  } catch (e) {
    message.error(e.message || "保存失败");
  } finally {
    saving.value = false;
  }
};

const removeTask = async (task) => {
  if (!confirm(`确认删除任务：${task.title}？`)) return;
  try {
    await deleteTask(task.id);
    message.success("已删除");
    await loadTasks();
  } catch (e) {
    message.error(e.message || "删除失败");
  }
};

const changeStatus = async (task, status) => {
  try {
    await updateTask(task.id, { ...task, status });
    message.success("状态已更新");
    await loadTasks();
  } catch (e) {
    message.error(e.message || "更新失败");
  }
};

const ensureAttachmentsLoaded = async (taskId) => {
  if (attachments[taskId]?.loaded) return;
  attachments[taskId] = { items: [], loading: true, loaded: false };
  try {
    const res = await listAttachments(taskId);
    attachments[taskId].items = res.items || [];
    attachments[taskId].loaded = true;
  } catch (e) {
    message.error(e.message || "附件加载失败");
  } finally {
    attachments[taskId].loading = false;
  }
};

const handleUpload = async (taskId, event) => {
  const file = event.target.files?.[0];
  event.target.value = "";
  if (!file) return;
  if (file.size > 10 * 1024 * 1024) {
    message.error("文件超过 10MB");
    return;
  }
  const type = file.type;
  const isImage = type.startsWith("image/");
  if (!isImage && !allowedTypes.includes(type)) {
    message.error("不支持的文件类型");
    return;
  }
  try {
    await uploadAttachment(taskId, file);
    message.success("上传成功");
    attachments[taskId] = { items: [], loading: true, loaded: false };
    await ensureAttachmentsLoaded(taskId);
    await loadTasks();
  } catch (e) {
    message.error(e.message || "上传失败");
  }
};

const handleDownload = async (attachment) => {
  try {
    await downloadAttachment(attachment.id, attachment.originalName);
  } catch (e) {
    message.error(e.message || "下载失败");
  }
};

onMounted(loadTasks);
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-900">
    <div class="mx-auto flex max-w-5xl flex-col gap-4 p-6">
      <header class="flex items-center justify-between gap-3">
        <div>
          <p class="text-sm text-gray-500">本地任务管理 / SQLite 持久化</p>
          <h1 class="text-2xl font-semibold">任务列表</h1>
        </div>
        <button
          class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          @click="openCreate"
        >
          新建任务
        </button>
      </header>

      <section
        v-if="errorMsg"
        class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
      >
        <div class="flex items-center justify-between">
          <span>{{ errorMsg }}</span>
          <button class="text-blue-600" @click="loadTasks">重试</button>
        </div>
      </section>

      <section
        v-if="loading"
        class="rounded-lg border bg-white px-4 py-6 text-sm text-gray-500"
      >
        加载中…
      </section>

      <section
        v-else-if="!tasks.length"
        class="rounded-lg border bg-white px-6 py-10 text-center text-gray-500"
      >
        <p>还没有任务，点击“新建任务”开始吧。</p>
      </section>

      <section v-else class="flex flex-col gap-3">
        <article
          v-for="task in tasks"
          :key="task.id"
          class="rounded-lg border bg-white p-4 shadow-sm"
        >
          <div class="flex flex-wrap items-start justify-between gap-3">
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <span
                  class="rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-700"
                  >{{ task.status }}</span
                >
                <span v-if="task.dueDate" class="text-xs text-gray-500"
                  >截止：{{ task.dueDate }}</span
                >
              </div>
              <h2 class="mt-1 break-words text-lg font-semibold">
                {{ task.title }}
              </h2>
              <p v-if="task.description" class="mt-1 text-sm text-gray-600">
                {{ task.description }}
              </p>
              <p class="mt-1 text-xs text-gray-400">
                创建于 {{ task.createdAt }}
              </p>
            </div>
            <div class="flex flex-col items-end gap-2 text-sm">
              <select
                class="rounded border px-2 py-1 text-sm"
                :value="task.status"
                @change="(e) => changeStatus(task, e.target.value)"
              >
                <option value="todo">待办</option>
                <option value="doing">进行中</option>
                <option value="done">完成</option>
              </select>
              <div class="flex gap-2">
                <button class="text-blue-600" @click="openEdit(task)">
                  编辑
                </button>
                <button class="text-red-600" @click="removeTask(task)">
                  删除
                </button>
              </div>
            </div>
          </div>

          <div class="mt-3 flex flex-col gap-2 border-t pt-3 text-sm">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-medium"
                  >附件（{{ task.attachmentCount }}）</span
                >
                <button
                  class="text-blue-600"
                  @click="ensureAttachmentsLoaded(task.id)"
                >
                  查看
                </button>
              </div>
              <label class="cursor-pointer text-blue-600">
                上传
                <input
                  type="file"
                  class="hidden"
                  @change="(e) => handleUpload(task.id, e)"
                />
              </label>
            </div>
            <div v-if="attachments[task.id]?.loading" class="text-gray-500">
              附件加载中…
            </div>
            <ul
              v-else-if="attachments[task.id]?.items?.length"
              class="flex flex-col gap-1"
            >
              <li
                v-for="att in attachments[task.id].items"
                :key="att.id"
                class="flex items-center justify-between rounded border px-2 py-1"
              >
                <div class="flex flex-col">
                  <span class="font-medium">{{ att.originalName }}</span>
                  <span class="text-xs text-gray-500"
                    >{{ att.mimeType }} ·
                    {{ (att.size / 1024).toFixed(1) }} KB</span
                  >
                </div>
                <button
                  class="text-blue-600"
                  @click="() => handleDownload(att)"
                >
                  下载
                </button>
              </li>
            </ul>
            <div v-else class="text-gray-500">暂无附件</div>
          </div>
        </article>
      </section>
    </div>

    <div
      v-if="formVisible"
      class="fixed inset-0 z-10 flex items-center justify-center bg-black/40 px-4"
    >
      <div class="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
        <h3 class="text-lg font-semibold">
          {{ editingId ? "编辑任务" : "新建任务" }}
        </h3>
        <div class="mt-4 flex flex-col gap-3 text-sm">
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">标题*</span>
            <input
              v-model="form.title"
              class="rounded border px-3 py-2"
              placeholder="请输入标题"
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">描述</span>
            <textarea
              v-model="form.description"
              rows="3"
              class="rounded border px-3 py-2"
              placeholder="补充描述"
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">状态</span>
            <select v-model="form.status" class="rounded border px-3 py-2">
              <option value="todo">待办</option>
              <option value="doing">进行中</option>
              <option value="done">完成</option>
            </select>
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">截止时间 (ISO)</span>
            <input
              v-model="form.dueDate"
              class="rounded border px-3 py-2"
              placeholder="2024-12-31T12:00:00Z"
            />
          </label>
        </div>
        <div class="mt-6 flex justify-end gap-3 text-sm">
          <button class="rounded px-4 py-2" @click="formVisible = false">
            取消
          </button>
          <button
            class="rounded bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 disabled:opacity-60"
            :disabled="saving"
            @click="saveTask"
          >
            {{ saving ? "保存中…" : "保存" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
