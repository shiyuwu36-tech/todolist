<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import {
  createTask,
  deleteTask,
  downloadAttachment,
  getTasks,
  listAttachments,
  updateTask,
  uploadAttachment,
} from "./apiClient";
import { createDiscreteApi, NDatePicker } from "naive-ui";

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
  dueDateValue: null, // timestamp(ms)
});

const searchInput = ref("");
const searchQuery = ref("");
const dateFilterValue = ref(null); // timestamp(ms)
const dateFilterMode = ref("all"); // all | on | next7

const allowedTypes = ["application/pdf", "text/plain"];
let searchTimer = null;

watch(searchInput, (val) => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    searchQuery.value = val;
  }, 300);
});

const resetForm = () => {
  editingId.value = null;
  form.title = "";
  form.description = "";
  form.status = "todo";
  form.dueDateValue = null;
};

const clearFilters = () => {
  searchInput.value = "";
  searchQuery.value = "";
  dateFilterMode.value = "all";
  dateFilterValue.value = null;
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
  form.dueDateValue = task.dueDate ? Date.parse(task.dueDate) : null;
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

const toIsoString = (ts) => (ts ? new Date(ts).toISOString() : null);
const formatDateTime = (iso) => {
  if (!iso) return "未设置";
  try {
    return new Intl.DateTimeFormat(undefined, {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(iso));
  } catch (e) {
    return iso;
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
      dueDate: toIsoString(form.dueDateValue),
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

const matchesSearch = (task) => {
  const q = searchQuery.value.trim().toLowerCase();
  if (!q) return true;
  const text = `${task.title || ""} ${task.description || ""}`.toLowerCase();
  return text.includes(q);
};

const matchesDateFilter = (task) => {
  const mode = dateFilterMode.value;
  if (mode === "all") return true;
  const targetTs = dateFilterValue.value;
  const due = task.dueDate ? Date.parse(task.dueDate) : null;
  if (!due) return false;
  if (mode === "on") {
    if (!targetTs) return true;
    const d = new Date(due);
    const t = new Date(targetTs);
    return (
      d.getFullYear() === t.getFullYear() &&
      d.getMonth() === t.getMonth() &&
      d.getDate() === t.getDate()
    );
  }
  if (mode === "next7") {
    const now = new Date();
    const end = new Date();
    end.setDate(now.getDate() + 7);
    return due >= now.getTime() && due <= end.getTime();
  }
  return true;
};

const filteredTasks = computed(() =>
  tasks.value.filter((t) => matchesSearch(t) && matchesDateFilter(t)),
);

const columns = computed(() => ({
  todo: filteredTasks.value.filter((t) => t.status === "todo"),
  doing: filteredTasks.value.filter((t) => t.status === "doing"),
  done: filteredTasks.value.filter((t) => t.status === "done"),
}));

onMounted(loadTasks);
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-900">
    <div class="mx-auto flex max-w-6xl flex-col gap-4 p-6">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p class="text-sm text-gray-500">本地可用 · 自动保存 · 支持附件</p>
          <h1 class="text-2xl font-semibold">任务看板 TodoList</h1>
        </div>
        <button
          class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          @click="openCreate"
        >
          新建任务
        </button>
      </header>

      <section class="rounded-lg border bg-white p-4 shadow-sm">
        <div class="flex flex-wrap items-center gap-3">
          <div class="flex min-w-[240px] flex-1 items-center gap-2">
            <input
              v-model="searchInput"
              type="search"
              class="w-full rounded border px-3 py-2 text-sm"
              placeholder="搜索标题或描述"
            />
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">到期过滤</span>
            <select
              v-model="dateFilterMode"
              class="rounded border px-3 py-2 text-sm"
            >
              <option value="all">全部</option>
              <option value="on">选定日期</option>
              <option value="next7">未来7天</option>
            </select>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">日期</span>
            <n-date-picker
              v-model:value="dateFilterValue"
              type="date"
              clearable
              class="w-44"
              placeholder="选择日期"
            />
          </div>
          <button
            class="rounded-lg border px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
            @click="clearFilters"
          >
            清空筛选
          </button>
        </div>
      </section>

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

      <section v-else class="grid gap-4 lg:grid-cols-3">
        <div
          v-for="status in ['todo', 'doing', 'done']"
          :key="status"
          class="flex flex-col gap-3 rounded-xl border bg-white p-4 shadow-sm"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="text-base font-semibold">{{
                status === "todo"
                  ? "待办"
                  : status === "doing"
                    ? "进行中"
                    : "已完成"
              }}</span>
              <span
                class="rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-600"
                >{{ columns[status].length }}</span
              >
            </div>
          </div>
          <div
            v-if="!columns[status].length"
            class="rounded-lg border border-dashed bg-gray-50 px-3 py-4 text-center text-sm text-gray-500"
          >
            此列暂无任务，可将任务切换到此状态
          </div>
          <article
            v-for="task in columns[status]"
            :key="task.id"
            class="rounded-lg border bg-white px-3 py-3 text-sm shadow-sm"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0 space-y-1">
                <h2 class="break-words text-base font-semibold text-gray-900">
                  {{ task.title }}
                </h2>
                <p v-if="task.description" class="text-gray-600">
                  {{ task.description }}
                </p>
                <div class="text-xs text-gray-500 space-y-0.5">
                  <p>创建：{{ formatDateTime(task.createdAt) }}</p>
                  <p>截止：{{ formatDateTime(task.dueDate) }}</p>
                  <p>附件：{{ task.attachmentCount }}</p>
                </div>
              </div>
              <select
                class="rounded border px-2 py-1 text-xs"
                :value="task.status"
                @change="(e) => changeStatus(task, e.target.value)"
              >
                <option value="todo">待办</option>
                <option value="doing">进行中</option>
                <option value="done">已完成</option>
              </select>
            </div>
            <div class="mt-3 flex flex-wrap gap-2 text-xs">
              <button
                class="rounded bg-blue-50 px-3 py-1 text-blue-700"
                @click="openEdit(task)"
              >
                编辑
              </button>
              <button
                class="rounded bg-red-50 px-3 py-1 text-red-700"
                @click="removeTask(task)"
              >
                删除
              </button>
              <button
                class="rounded bg-gray-100 px-3 py-1 text-gray-700"
                @click="() => ensureAttachmentsLoaded(task.id)"
              >
                附件 ({{ task.attachmentCount }})
              </button>
            </div>
            <div class="mt-2 rounded border bg-gray-50 p-2">
              <div class="flex items-center justify-between text-xs">
                <span class="font-medium text-gray-700">附件</span>
                <label
                  class="cursor-pointer rounded bg-blue-50 px-2 py-1 text-blue-700"
                >
                  上传
                  <input
                    type="file"
                    class="hidden"
                    @change="(e) => handleUpload(task.id, e)"
                  />
                </label>
              </div>
              <div
                v-if="attachments[task.id]?.loading"
                class="mt-1 text-gray-500"
              >
                附件加载中…
              </div>
              <ul
                v-else-if="attachments[task.id]?.items?.length"
                class="mt-2 flex flex-col gap-1"
              >
                <li
                  v-for="att in attachments[task.id].items"
                  :key="att.id"
                  class="flex items-center justify-between rounded border bg-white px-2 py-1"
                >
                  <div class="flex flex-col">
                    <span class="font-medium">{{ att.originalName }}</span>
                    <span class="text-[11px] text-gray-500"
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
              <div v-else class="mt-1 text-gray-500">暂无附件</div>
            </div>
          </article>
        </div>
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
              <option value="done">已完成</option>
            </select>
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">截止时间</span>
            <n-date-picker
              v-model:value="form.dueDateValue"
              type="datetime"
              clearable
              placeholder="选择时间"
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
