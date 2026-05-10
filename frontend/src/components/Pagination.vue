<template>
  <div class="pagination">
    <div class="pagination-info">
      共 <span class="total">{{ total }}</span> 条记录，第 <span class="page">{{ page }}</span> / <span class="total-pages">{{ totalPages }}</span> 页
    </div>
    <div class="pagination-buttons">
      <button
        class="page-btn"
        :disabled="page <= 1"
        @click="goToPage(1)"
      >
        ⏮️ 首页
      </button>
      <button
        class="page-btn"
        :disabled="page <= 1"
        @click="goToPage(page - 1)"
      >
        ◀️
      </button>
      <button
        v-for="p in visiblePages"
        :key="p"
        class="page-btn"
        :class="{ active: p === page }"
        @click="goToPage(p)"
      >
        {{ p }}
      </button>
      <button
        class="page-btn"
        :disabled="page >= totalPages"
        @click="goToPage(page + 1)"
      >
        ▶️
      </button>
      <button
        class="page-btn"
        :disabled="page >= totalPages"
        @click="goToPage(totalPages)"
      >
        末页 ⏭️
      </button>
      <select class="page-size-select" @change="changePageSize" :value="pageSize">
        <option :value="5">5条/页</option>
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  },
  totalPages: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['update:page', 'update:pageSize', 'change'])

const visiblePages = computed(() => {
  const pages = []
  const totalPages = props.totalPages
  const page = props.page
  let start = Math.max(1, page - 2)
  let end = Math.min(totalPages, start + 4)
  
  if (end - start < 4) {
    start = Math.max(1, end - 4)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const goToPage = (p) => {
  if (p >= 1 && p <= props.totalPages && p !== props.page) {
    emit('update:page', p)
    emit('change', { page: p, pageSize: props.pageSize })
  }
}

const changePageSize = (e) => {
  const size = parseInt(e.target.value)
  emit('update:pageSize', size)
  emit('update:page', 1)
  emit('change', { page: 1, pageSize: size })
}
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
  background: #fafafa;
}

.pagination-info {
  font-size: 14px;
  color: var(--text-secondary);
}

.pagination-info .total,
.pagination-info .page,
.pagination-info .total-pages {
  font-weight: 600;
  color: var(--primary-color);
}

.pagination-buttons {
  display: flex;
  align-items: center;
  gap: 6px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-primary);
  transition: var(--transition);
  min-width: 36px;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.page-btn.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-size-select {
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: white;
  font-size: 13px;
  cursor: pointer;
  margin-left: 8px;
}

.page-size-select:focus {
  outline: none;
  border-color: var(--primary-color);
}
</style>
