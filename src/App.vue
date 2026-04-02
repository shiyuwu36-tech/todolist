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
import { createDiscreteApi, NDatePicker, NPopover } from "naive-ui";

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
  priority: "medium",
  dueDateValue: null,
});

const searchInput = ref("");
const searchQuery = ref("");
const dateFilterValue = ref(null);
const dateFilterMode = ref("all");
const priorityFilter = ref("all");
const prioritySort = ref("desc"); // 默认高->低
const moreFiltersOpen = ref(false);

const activeTab = ref("board");

const draggingTaskId = ref(null);
const dragOriginStatus = ref(null);
const dragOverStatus = ref(null);

const calendarMonth = ref(new Date());
const calendarDrawerOpen = ref(false);
const selectedDate = ref(null);
const selectedDateTasks = ref([]);

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
  form.priority = "medium";
  form.dueDateValue = null;
};

const resetSort = () => {
  prioritySort.value = "desc";
};

const clearFilters = () => {
  searchInput.value = "";
  searchQuery.value = "";
  dateFilterMode.value = "all";
  dateFilterValue.value = null;
  priorityFilter.value = "all";
  resetSort();
  moreFiltersOpen.value = false;
};

const clearDateFilter = () => {
  dateFilterMode.value = "all";
  dateFilterValue.value = null;
};

const clearPriorityFilter = () => {
  priorityFilter.value = "all";
};

const clearSearch = () => {
  searchInput.value = "";
  searchQuery.value = "";
  clearDateFilter();
  clearPriorityFilter();
  resetSort();
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
  form.priority = task.priority || "medium";
  form.dueDateValue = task.dueDate ? Date.parse(task.dueDate) : null;
  formVisible.value = true;
};

const loadTasks = async () => {
  loading.value = true;
  errorMsg.value = "";
  try {
    const res = await getTasks();
    tasks.value = res.items || [];
    const ids = new Set(tasks.value.map((t) => t.id));
    Object.keys(attachments).forEach((key) => {
      if (!ids.has(key)) {
        delete attachments[key];
      }
    });
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
      priority: form.priority,
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
    await updateTask(task.id, { ...task, status, priority: task.priority });
    message.success("状态已更新");
    await loadTasks();
  } catch (e) {
    message.error(e.message || "更新失败");
  }
};

const ensureAttachmentsLoaded = async (taskId) => {
  if (attachments[taskId]?.loaded) return;
  attachments[taskId] = {
    ...(attachments[taskId] || {}),
    items: [],
    loading: true,
    loaded: false,
  };
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

const loadVisibleAttachments = async (list) => {
  const tasksWithAtt = list.filter((t) => t.attachmentCount > 0);
  for (const t of tasksWithAtt) {
    if (!attachments[t.id]?.loaded) {
      await ensureAttachmentsLoaded(t.id);
    }
  }
  const tasksWithoutAtt = list.filter((t) => t.attachmentCount === 0);
  tasksWithoutAtt.forEach((t) => {
    if (!attachments[t.id]) {
      attachments[t.id] = { items: [], loading: false, loaded: true };
    }
  });
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

const matchesPriority = (task) => {
  if (priorityFilter.value === "all") return true;
  return task.priority === priorityFilter.value;
};

const priorityWeight = { high: 3, medium: 2, low: 1 };

const filteredTasks = computed(() => {
  const list = tasks.value.filter(
    (t) => matchesSearch(t) && matchesDateFilter(t) && matchesPriority(t),
  );
  return [...list].sort((a, b) => {
    const wa = priorityWeight[a.priority] || 0;
    const wb = priorityWeight[b.priority] || 0;
    if (prioritySort.value === "desc") return wb - wa;
    return wa - wb;
  });
});

const columns = computed(() => ({
  todo: filteredTasks.value.filter((t) => t.status === "todo"),
  doing: filteredTasks.value.filter((t) => t.status === "doing"),
  done: filteredTasks.value.filter((t) => t.status === "done"),
}));

const statusTheme = {
  todo: {
    column: "bg-gradient-to-b from-sky-100 to-sky-50 border-sky-200",
    tag: "bg-sky-100 text-sky-800",
  },
  doing: {
    column: "bg-gradient-to-b from-amber-100 to-amber-50 border-amber-200",
    tag: "bg-amber-100 text-amber-800",
  },
  done: {
    column:
      "bg-gradient-to-b from-emerald-100 to-emerald-50 border-emerald-200",
    tag: "bg-emerald-100 text-emerald-800",
  },
};

const priorityTheme = {
  high: "bg-red-100 text-red-800",
  medium: "bg-amber-100 text-amber-800",
  low: "bg-emerald-100 text-emerald-800",
};

const formattedDateFilter = computed(() => {
  if (dateFilterMode.value === "all") return "";
  if (dateFilterMode.value === "next7") return "未来7天";
  if (dateFilterMode.value === "on" && dateFilterValue.value) {
    return new Intl.DateTimeFormat(undefined, {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(new Date(dateFilterValue.value));
  }
  return "选定日期";
});

const hasActiveChips = computed(() => {
  return (
    searchQuery.value.trim() ||
    dateFilterMode.value !== "all" ||
    dateFilterValue.value ||
    priorityFilter.value !== "all" ||
    prioritySort.value !== "desc"
  );
});

const dueTone = (iso) => {
  if (!iso) return "";
  const now = Date.now();
  const ts = Date.parse(iso);
  if (Number.isNaN(ts)) return "";
  const diff = ts - now;
  if (diff < 0) return "text-red-600";
  if (diff < 24 * 60 * 60 * 1000) return "text-amber-600";
  return "text-gray-600";
};

const toDayKey = (input) => {
  if (!input) return null;
  const d = new Date(input);
  if (Number.isNaN(d.getTime())) return null;
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
};

const calendarTasks = computed(() =>
  filteredTasks.value.filter((t) => Boolean(t.dueDate)),
);

const tasksByDay = computed(() => {
  const map = new Map();
  calendarTasks.value.forEach((t) => {
    const key = toDayKey(t.dueDate);
    if (!key) return;
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(t);
  });
  return map;
});

const currentMonthLabel = computed(() => {
  const formatter = new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "long",
  });
  return formatter.format(new Date(calendarMonth.value));
});

const selectedDateLabel = computed(() => {
  if (!selectedDate.value) return "";
  return new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(selectedDate.value);
});

const calendarDays = computed(() => {
  const first = new Date(calendarMonth.value);
  first.setDate(1);
  const startOffset = (first.getDay() + 6) % 7; // 周一为起点
  const start = new Date(first);
  start.setDate(first.getDate() - startOffset);

  const days = [];
  for (let i = 0; i < 42; i += 1) {
    const current = new Date(start);
    current.setDate(start.getDate() + i);
    const key = toDayKey(current);
    const inMonth = current.getMonth() === first.getMonth();
    const dayTasks = tasksByDay.value.get(key) || [];
    days.push({ date: current, key, inMonth, tasks: dayTasks });
  }
  return days;
});

const goPrevMonth = () => {
  const d = new Date(calendarMonth.value);
  d.setMonth(d.getMonth() - 1);
  calendarMonth.value = d;
};

const goNextMonth = () => {
  const d = new Date(calendarMonth.value);
  d.setMonth(d.getMonth() + 1);
  calendarMonth.value = d;
};

const openCalendarDay = async (day) => {
  selectedDate.value = day.date;
  selectedDateTasks.value = day.tasks;
  calendarDrawerOpen.value = true;
  const tasksNeedAtt = day.tasks.filter((t) => t.attachmentCount > 0);
  for (const t of tasksNeedAtt) {
    await ensureAttachmentsLoaded(t.id);
  }
};

const closeCalendarDrawer = () => {
  calendarDrawerOpen.value = false;
  selectedDate.value = null;
  selectedDateTasks.value = [];
};

const moveTaskLocal = (taskId, status) => {
  tasks.value = tasks.value.map((t) =>
    t.id === taskId
      ? {
          ...t,
          status,
        }
      : t,
  );
};

const handleDragStart = (event, task) => {
  draggingTaskId.value = task.id;
  dragOriginStatus.value = task.status;
  dragOverStatus.value = null;
  event.dataTransfer?.setData("text/plain", task.id);
};

const handleDragOver = (status) => {
  if (draggingTaskId.value) {
    dragOverStatus.value = status;
  }
};

const handleDragLeave = () => {
  dragOverStatus.value = null;
};

const handleDrop = async (status) => {
  const taskId = draggingTaskId.value;
  const originStatus = dragOriginStatus.value;
  dragOverStatus.value = null;
  if (!taskId) return;
  const task = tasks.value.find((t) => t.id === taskId);
  if (!task) {
    draggingTaskId.value = null;
    return;
  }
  if (task.status === status) {
    draggingTaskId.value = null;
    dragOriginStatus.value = null;
    return;
  }

  moveTaskLocal(taskId, status);
  try {
    await updateTask(taskId, { ...task, status, priority: task.priority });
    await loadTasks();
    message.success("状态已更新");
  } catch (e) {
    moveTaskLocal(taskId, originStatus || task.status);
    await loadTasks();
    message.error(e.message || "拖拽更新失败");
  } finally {
    draggingTaskId.value = null;
    dragOriginStatus.value = null;
  }
};

const handleDragEnd = () => {
  draggingTaskId.value = null;
  dragOriginStatus.value = null;
  dragOverStatus.value = null;
};

watch(
  () => filteredTasks.value,
  async (list) => {
    await loadVisibleAttachments(list);
  },
  { immediate: true },
);

watch([tasks, selectedDate], () => {
  if (!selectedDate.value) return;
  const key = toDayKey(selectedDate.value);
  selectedDateTasks.value = tasksByDay.value.get(key) || [];
});

onMounted(loadTasks);
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-br from-slate-100 via-sky-100 to-indigo-100 text-gray-900"
    style="
      background-image:
        radial-gradient(
          720px at 16% 20%,
          rgba(56, 189, 248, 0.16),
          transparent 58%
        ),
        radial-gradient(
          640px at 78% 78%,
          rgba(16, 185, 129, 0.14),
          transparent 60%
        );
    "
  >
    <div
      class="mx-auto flex max-w-[1400px] flex-col gap-5 px-4 py-6 sm:px-6 lg:px-8"
    >
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p class="text-xs text-gray-500">本地可用 · 自动保存 · 支持附件</p>
          <h1 class="text-4xl font-extrabold tracking-tight text-slate-900">
            任务看板 TodoList
          </h1>
        </div>
        <button
          class="rounded-full bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow hover:bg-blue-700"
          @click="openCreate"
        >
          新建任务
        </button>
      </header>

      <div class="flex items-center gap-2 text-sm">
        <button
          class="rounded-lg px-3 py-2 font-semibold shadow-sm"
          :class="
            activeTab === 'board'
              ? 'bg-blue-600 text-white shadow'
              : 'bg-white text-gray-700 border border-slate-200'
          "
          @click="activeTab = 'board'"
        >
          看板
        </button>
        <button
          class="rounded-lg px-3 py-2 font-semibold shadow-sm"
          :class="
            activeTab === 'calendar'
              ? 'bg-blue-600 text-white shadow'
              : 'bg-white text-gray-700 border border-slate-200'
          "
          @click="activeTab = 'calendar'"
        >
          月历
        </button>
        <span class="text-xs text-gray-500">
          未设置截止时间的任务仅在看板显示；筛选条件在看板内保持。
        </span>
      </div>

      <section
        class="rounded-2xl border border-slate-200 bg-white/85 p-4 shadow-sm backdrop-blur"
      >
        <div class="relative z-20 flex flex-wrap items-center gap-3">
          <div class="flex min-w-[240px] flex-1 items-center gap-2 text-sm">
            <span class="text-slate-400">🔍</span>
            <input
              v-model="searchInput"
              type="search"
              class="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm shadow-inner focus:border-blue-400 focus:outline-none"
              placeholder="搜索标题或描述"
            />
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs font-medium text-gray-500">到期</span>
            <select
              v-model="dateFilterMode"
              class="rounded-lg border border-slate-200 px-3 py-2 text-sm shadow-inner focus:border-blue-400 focus:outline-none"
            >
              <option value="all">全部</option>
              <option value="on">选定日期</option>
              <option value="next7">未来7天</option>
            </select>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs font-medium text-gray-500">日期</span>
            <n-date-picker
              v-model:value="dateFilterValue"
              type="date"
              clearable
              class="w-40"
              placeholder="选择日期"
            />
          </div>
          <button
            class="rounded-lg border border-slate-200 px-3 py-2 text-sm font-medium text-gray-700 shadow hover:bg-slate-50"
            @click="clearFilters"
          >
            清空筛选
          </button>
          <n-popover
            trigger="click"
            placement="bottom-end"
            :show="moreFiltersOpen"
            :z-index="3500"
            :width="260"
            to="body"
            @update:show="(val) => (moreFiltersOpen = val)"
          >
            <template #trigger>
              <button
                class="rounded-lg border border-slate-200 px-3 py-2 text-sm font-medium text-gray-700 shadow hover:bg-slate-50"
              >
                更多筛选
              </button>
            </template>
            <div class="flex flex-col gap-3 text-sm">
              <label class="flex flex-col gap-1">
                <span class="text-xs font-medium text-gray-600">优先级</span>
                <select
                  v-model="priorityFilter"
                  class="rounded-lg border border-slate-200 px-3 py-2 text-sm shadow-inner focus:border-blue-400 focus:outline-none"
                >
                  <option value="all">全部</option>
                  <option value="high">高</option>
                  <option value="medium">中</option>
                  <option value="low">低</option>
                </select>
              </label>
              <label class="flex flex-col gap-1">
                <span class="text-xs font-medium text-gray-600">排序</span>
                <select
                  v-model="prioritySort"
                  class="rounded-lg border border-slate-200 px-3 py-2 text-sm shadow-inner focus:border-blue-400 focus:outline-none"
                >
                  <option value="desc">高 → 低</option>
                  <option value="asc">低 → 高</option>
                </select>
              </label>
              <div class="flex justify-end gap-2 text-xs">
                <button
                  class="rounded-lg px-3 py-1 text-gray-600 hover:bg-slate-100"
                  @click="clearPriorityFilter"
                >
                  重置优先级
                </button>
                <button
                  class="rounded-lg px-3 py-1 text-gray-600 hover:bg-slate-100"
                  @click="resetSort"
                >
                  恢复排序默认
                </button>
              </div>
            </div>
          </n-popover>
        </div>

        <div
          v-if="hasActiveChips"
          class="mt-3 flex flex-wrap items-center gap-2 text-[12px]"
        >
          <span class="text-gray-500">已应用：</span>
          <button
            v-if="searchQuery.trim()"
            class="flex items-center gap-1 rounded-full bg-slate-100 px-3 py-1 text-gray-700 shadow-sm hover:bg-slate-200"
            @click="clearSearch"
          >
            🔍 {{ searchQuery }} ×
          </button>
          <button
            v-if="dateFilterMode !== 'all' || dateFilterValue"
            class="flex items-center gap-1 rounded-full bg-slate-100 px-3 py-1 text-gray-700 shadow-sm hover:bg-slate-200"
            @click="clearDateFilter"
          >
            📅 {{ formattedDateFilter }} ×
          </button>
          <button
            v-if="priorityFilter !== 'all'"
            class="flex items-center gap-1 rounded-full bg-slate-100 px-3 py-1 text-gray-700 shadow-sm hover:bg-slate-200"
            @click="clearPriorityFilter"
          >
            ⭐ 优先级：{{ priorityFilter }} ×
          </button>
          <button
            v-if="prioritySort !== 'desc'"
            class="flex items-center gap-1 rounded-full bg-slate-100 px-3 py-1 text-gray-700 shadow-sm hover:bg-slate-200"
            @click="resetSort"
          >
            ↕ 排序：低→高 ×
          </button>
        </div>
      </section>

      <section
        v-if="errorMsg"
        class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
      >
        <div class="flex items-center justify-between">
          <span>{{ errorMsg }}</span>
          <button class="text-blue-600" @click="loadTasks">重试</button>
        </div>
      </section>

      <section
        v-if="loading"
        class="rounded-xl border bg-white px-4 py-6 text-sm text-gray-500 shadow"
      >
        加载中…
      </section>

      <section
        v-else-if="activeTab === 'board'"
        class="grid gap-5 lg:grid-cols-3"
      >
        <div
          v-for="status in ['todo', 'doing', 'done']"
          :key="status"
          class="relative flex flex-col gap-3 rounded-2xl border border-black/5 p-4 shadow-sm min-h-[520px] lg:max-h-[calc(100vh-240px)] lg:overflow-hidden"
          :class="[
            statusTheme[status].column,
            dragOverStatus === status ? 'ring-2 ring-blue-300' : '',
          ]"
          @dragover.prevent="handleDragOver(status)"
          @dragleave="handleDragLeave"
          @drop.prevent="handleDrop(status)"
        >
          <div
            class="sticky top-0 z-10 flex items-center justify-between rounded-xl bg-white/85 px-3 py-2 shadow-sm ring-1 ring-white/50 backdrop-blur"
          >
            <div class="flex items-center gap-2">
              <span class="text-lg font-semibold text-gray-900">{{
                status === "todo"
                  ? "待办"
                  : status === "doing"
                    ? "进行中"
                    : "已完成"
              }}</span>
              <span
                class="rounded-full bg-gray-900/5 px-2 py-1 text-[11px] font-semibold text-gray-700"
                >{{ columns[status].length }}</span
              >
            </div>
            <span class="text-[11px] text-gray-500">保持节奏</span>
          </div>

          <div
            v-if="!columns[status].length"
            class="rounded-xl border border-dashed border-slate-200 bg-white/70 px-3 py-4 text-center text-sm text-gray-600 shadow-inner"
          >
            <span v-if="status === 'todo'"
              >此列暂无任务，点击右上角「新建任务」开始。</span
            >
            <span v-else>可通过任务卡右上角状态切换移入此列。</span>
          </div>

          <div class="flex flex-col gap-3 lg:overflow-y-auto lg:pr-1">
            <article
              v-for="task in columns[status]"
              :key="task.id"
              class="rounded-xl border border-slate-100 bg-white/85 p-3 text-sm leading-relaxed shadow transition duration-200 hover:-translate-y-0.5 hover:shadow-lg backdrop-blur-sm"
              :class="{
                'opacity-80 ring-1 ring-blue-300': draggingTaskId === task.id,
              }"
              draggable="true"
              @dragstart="(e) => handleDragStart(e, task)"
              @dragend="handleDragEnd"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0 space-y-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <span
                      class="rounded-full px-2 py-1 text-xs font-semibold"
                      :class="statusTheme[task.status]?.tag"
                    >
                      {{
                        task.status === "todo"
                          ? "待办"
                          : task.status === "doing"
                            ? "进行中"
                            : "已完成"
                      }}
                    </span>
                    <span
                      class="rounded-full px-2 py-1 text-xs font-semibold"
                      :class="
                        priorityTheme[task.priority] ||
                        'bg-slate-100 text-slate-700'
                      "
                    >
                      {{
                        task.priority === "high"
                          ? "高优先级"
                          : task.priority === "low"
                            ? "低优先级"
                            : "中优先级"
                      }}
                    </span>
                    <span class="text-xs text-gray-500"
                      >#{{ task.id.slice(0, 6) }}</span
                    >
                  </div>
                  <h2
                    class="break-words text-lg font-semibold leading-tight text-gray-900 md:text-xl"
                  >
                    {{ task.title }}
                  </h2>
                  <p
                    v-if="task.description"
                    class="text-[13px] leading-snug text-gray-500"
                    style="
                      display: -webkit-box;
                      -webkit-line-clamp: 2;
                      -webkit-box-orient: vertical;
                      overflow: hidden;
                    "
                  >
                    {{ task.description }}
                  </p>
                </div>
                <select
                  class="rounded-md border border-slate-200 px-2 py-1 text-xs text-gray-700"
                  :value="task.status"
                  @change="(e) => changeStatus(task, e.target.value)"
                >
                  <option value="todo">待办</option>
                  <option value="doing">进行中</option>
                  <option value="done">已完成</option>
                </select>
              </div>

              <div
                class="mt-2 flex flex-wrap gap-2 text-[12px] font-medium text-gray-500"
              >
                <span
                  class="flex items-center gap-1 rounded bg-slate-100 px-2 py-1"
                  ><span>📅</span
                  ><span :class="dueTone(task.dueDate)"
                    >截止：{{ formatDateTime(task.dueDate) }}</span
                  ></span
                >
                <span
                  class="flex items-center gap-1 rounded bg-slate-100 px-2 py-1"
                  >🕒 创建：{{ formatDateTime(task.createdAt) }}</span
                >
              </div>

              <div class="mt-3 flex flex-wrap gap-2 text-xs">
                <div class="flex flex-wrap gap-2">
                  <button
                    class="flex items-center gap-1 rounded-md bg-blue-50 px-3 py-1 font-medium text-blue-700 hover:bg-blue-100"
                    @click="openEdit(task)"
                  >
                    ✏️ 编辑
                  </button>
                  <button
                    class="flex items-center gap-1 rounded-md bg-red-50 px-3 py-1 font-medium text-red-700 hover:bg-red-100"
                    @click="removeTask(task)"
                  >
                    🗑 删除
                  </button>
                </div>
                <div
                  class="flex flex-wrap gap-2 text-[12px] font-medium text-gray-600"
                >
                  <div
                    class="flex items-center gap-1 rounded-md bg-slate-100 px-3 py-1"
                    aria-hidden="true"
                  >
                    📂 附件
                    <span class="font-semibold">{{
                      task.attachmentCount
                    }}</span>
                  </div>
                  <label
                    class="flex cursor-pointer items-center gap-1 rounded-md bg-emerald-50 px-3 py-1 font-medium text-emerald-700 hover:bg-emerald-100"
                  >
                    ⬆️ 上传
                    <input
                      type="file"
                      class="hidden"
                      @change="(e) => handleUpload(task.id, e)"
                    />
                  </label>
                </div>
              </div>

              <div
                class="mt-2 rounded-lg border border-slate-100 bg-slate-50 p-2"
              >
                <div
                  class="flex items-center justify-between text-[12px] font-medium text-gray-600"
                >
                  <span class="font-semibold">附件列表</span>
                  <span class="text-gray-500">
                    {{
                      attachments[task.id]?.items?.length ??
                      task.attachmentCount
                    }}
                    个
                  </span>
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
                    class="flex items-center justify-between rounded border border-slate-100 bg-white px-2 py-1"
                  >
                    <div class="flex flex-col">
                      <span class="font-medium text-gray-800">{{
                        att.originalName
                      }}</span>
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
        </div>
      </section>

      <section
        v-else
        class="rounded-2xl border border-slate-200 bg-white/85 p-4 shadow-sm backdrop-blur"
      >
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="text-xs text-gray-500">
              月历视图仅展示已设置截止时间的任务，当前筛选已应用。
            </p>
            <h2 class="text-xl font-semibold text-gray-900">
              {{ currentMonthLabel }}
            </h2>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <button
              class="rounded-lg border border-slate-200 px-3 py-1 text-gray-700 hover:bg-slate-50"
              @click="goPrevMonth"
            >
              上一月
            </button>
            <button
              class="rounded-lg border border-slate-200 px-3 py-1 text-gray-700 hover:bg-slate-50"
              @click="goNextMonth"
            >
              下一月
            </button>
          </div>
        </div>

        <div class="mt-4 overflow-x-auto">
          <div class="min-w-[760px] space-y-2">
            <div
              class="grid grid-cols-7 gap-2 text-center text-xs font-medium text-gray-500"
            >
              <span>一</span>
              <span>二</span>
              <span>三</span>
              <span>四</span>
              <span>五</span>
              <span>六</span>
              <span>日</span>
            </div>
            <div class="grid grid-cols-7 gap-2 text-sm">
              <button
                v-for="day in calendarDays"
                :key="day.key + day.inMonth"
                class="flex h-32 flex-col gap-1 rounded-xl border p-2 text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow"
                :class="[
                  day.inMonth
                    ? 'bg-white'
                    : 'bg-slate-50 text-gray-400 border-dashed',
                ]"
                @click="openCalendarDay(day)"
              >
                <div class="flex items-center justify-between text-xs">
                  <span
                    class="font-semibold"
                    :class="day.inMonth ? 'text-gray-800' : 'text-gray-400'"
                  >
                    {{ day.date.getDate() }}
                  </span>
                  <span
                    v-if="day.tasks.length"
                    class="rounded-full bg-blue-50 px-2 py-0.5 text-[11px] font-semibold text-blue-700"
                  >
                    {{ day.tasks.length }} 个任务
                  </span>
                </div>
                <div class="flex flex-col gap-1 text-[12px]">
                  <template v-if="day.tasks.length">
                    <span
                      v-for="t in day.tasks.slice(0, 2)"
                      :key="t.id"
                      class="truncate rounded bg-slate-100 px-2 py-1 text-gray-700"
                    >
                      {{ t.title }}
                    </span>
                    <span
                      v-if="day.tasks.length > 2"
                      class="text-[11px] text-gray-500"
                    >
                      +{{ day.tasks.length - 2 }} 更多
                    </span>
                  </template>
                  <span v-else class="text-[11px] text-gray-400">暂无任务</span>
                </div>
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div
      v-if="calendarDrawerOpen"
      class="fixed inset-0 z-10 flex justify-end bg-black/30"
    >
      <div
        class="h-full w-full max-w-xl overflow-y-auto rounded-l-2xl bg-white p-4 shadow-2xl"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-xs text-gray-500">当天任务列表</p>
            <h3 class="text-lg font-semibold text-gray-900">
              {{ selectedDateLabel || "" }}
            </h3>
          </div>
          <button class="text-sm text-gray-600" @click="closeCalendarDrawer">
            关闭
          </button>
        </div>

        <div class="mt-4 space-y-3">
          <div
            v-if="!selectedDateTasks.length"
            class="rounded-lg border border-dashed border-slate-200 px-3 py-4 text-sm text-gray-500"
          >
            当天暂无任务。
          </div>
          <article
            v-for="task in selectedDateTasks"
            :key="task.id"
            class="rounded-xl border border-slate-100 bg-white p-3 text-sm shadow-sm"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="min-w-0">
                <h4 class="text-base font-semibold text-gray-900">
                  {{ task.title }}
                </h4>
                <p v-if="task.description" class="text-xs text-gray-500">
                  {{ task.description }}
                </p>
              </div>
              <select
                class="rounded-md border border-slate-200 px-2 py-1 text-xs text-gray-700"
                :value="task.status"
                @change="(e) => changeStatus(task, e.target.value)"
              >
                <option value="todo">待办</option>
                <option value="doing">进行中</option>
                <option value="done">已完成</option>
              </select>
            </div>

            <div class="mt-2 flex flex-wrap gap-2 text-[12px] text-gray-500">
              <span
                class="flex items-center gap-1 rounded bg-slate-100 px-2 py-1"
                >📅 {{ formatDateTime(task.dueDate) }}</span
              >
              <span
                class="flex items-center gap-1 rounded bg-slate-100 px-2 py-1"
                >🕒 {{ formatDateTime(task.createdAt) }}</span
              >
            </div>

            <div class="mt-3 flex flex-wrap gap-2 text-xs">
              <button
                class="rounded-md bg-blue-50 px-3 py-1 font-medium text-blue-700 hover:bg-blue-100"
                @click="openEdit(task)"
              >
                编辑
              </button>
              <button
                class="rounded-md bg-red-50 px-3 py-1 font-medium text-red-700 hover:bg-red-100"
                @click="removeTask(task)"
              >
                删除
              </button>
            </div>

            <div
              class="mt-3 rounded-lg border border-slate-100 bg-slate-50 p-2"
            >
              <div
                class="flex items-center justify-between text-[12px] font-medium text-gray-600"
              >
                <span>附件</span>
                <span class="text-gray-500">
                  {{
                    attachments[task.id]?.items?.length ?? task.attachmentCount
                  }}
                  个
                </span>
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
                  class="flex items-center justify-between rounded border border-slate-100 bg-white px-2 py-1"
                >
                  <div class="flex flex-col">
                    <span class="font-medium text-gray-800">{{
                      att.originalName
                    }}</span>
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
              <label
                class="mt-2 inline-flex cursor-pointer items-center gap-1 rounded-md bg-emerald-50 px-3 py-1 text-[12px] font-medium text-emerald-700 hover:bg-emerald-100"
              >
                ⬆️ 上传
                <input
                  type="file"
                  class="hidden"
                  @change="(e) => handleUpload(task.id, e)"
                />
              </label>
            </div>
          </article>
        </div>
      </div>
    </div>

    <div
      v-if="formVisible"
      class="fixed inset-0 z-10 flex items-center justify-center bg-black/40 px-4"
    >
      <div class="w-full max-w-lg rounded-xl bg-white p-6 shadow-2xl">
        <h3 class="text-lg font-semibold">
          {{ editingId ? "编辑任务" : "新建任务" }}
        </h3>
        <div class="mt-4 flex flex-col gap-3 text-sm">
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">标题*</span>
            <input
              v-model="form.title"
              class="rounded-lg border border-slate-200 px-3 py-2 shadow-inner focus:border-blue-400 focus:outline-none"
              placeholder="请输入标题"
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">描述</span>
            <textarea
              v-model="form.description"
              rows="3"
              class="rounded-lg border border-slate-200 px-3 py-2 shadow-inner focus:border-blue-400 focus:outline-none"
              placeholder="补充描述"
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">状态</span>
            <select
              v-model="form.status"
              class="rounded-lg border border-slate-200 px-3 py-2 shadow-inner focus:border-blue-400 focus:outline-none"
            >
              <option value="todo">待办</option>
              <option value="doing">进行中</option>
              <option value="done">已完成</option>
            </select>
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-gray-700">优先级</span>
            <select
              v-model="form.priority"
              class="rounded-lg border border-slate-200 px-3 py-2 shadow-inner focus:border-blue-400 focus:outline-none"
            >
              <option value="high">高</option>
              <option value="medium">中</option>
              <option value="low">低</option>
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
          <button
            class="rounded-lg px-4 py-2 text-gray-700 hover:bg-gray-100"
            @click="formVisible = false"
          >
            取消
          </button>
          <button
            class="rounded-lg bg-blue-600 px-4 py-2 font-medium text-white shadow hover:bg-blue-700 disabled:opacity-60"
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
