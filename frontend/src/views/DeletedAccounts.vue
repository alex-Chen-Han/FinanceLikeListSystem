<template>
  <v-container>
    <v-card class="mb-4">
      <v-card-title>
        已刪除帳戶
        <v-spacer></v-spacer>
      </v-card-title>

      <v-data-table
        :headers="headers"
        :items="accounts"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.action="{ item }">
          <v-btn class="mr-2 esun-green-btn" size="small" @click="restoreAccount(item.userId)">
            復原
          </v-btn>
          <v-btn class="esun-green-btn" size="small" @click="hardDeleteAccount(item.userId)">
            永久刪除
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import api from '../utils/api'

const headers = [
  { title: '使用者 ID', key: 'userId' },
  { title: '姓名', key: 'userName' },
  { title: '電子郵件', key: 'email' },
  { title: '扣款帳號', key: 'account' },
  { title: '更新時間', key: 'updatedAt' },
  { title: '操作', key: 'action', sortable: false }
]

const accounts = ref([])
const loading = ref(false)

const fetchAccounts = async () => {
  loading.value = true
  try {
    const res = await api.get('/users/deleted')
    if (res.success) {
      accounts.value = res.data
    }
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

const showError = (text) => {
  const errMsg = text.response?.data?.message || text.message || String(text)
  Swal.fire({
    icon: 'error',
    title: '錯誤',
    text: errMsg
  })
}

const showSuccess = (text) => {
  Swal.fire({
    icon: 'success',
    title: '成功',
    text: text,
    timer: 1500,
    showConfirmButton: false
  })
}

const restoreAccount = async (userId) => {
  const result = await Swal.fire({
    title: '確認復原',
    text: '確定要復原這個扣款帳戶嗎？',
    icon: 'question',
    showCancelButton: true,
    customClass: {
      confirmButton: 'esun-green-btn',
      cancelButton: 'esun-cancel-btn'
    },
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const res = await api.patch(`/users/${userId}/restore`)
      if (res.success) {
        showSuccess('已復原扣款帳戶')
        fetchAccounts()
      } else {
        showError(res.message)
      }
    } catch (error) {
      showError(error)
    }
  }
}

const hardDeleteAccount = async (userId) => {
  const result = await Swal.fire({
    title: '永久刪除警告',
    text: '您確定要永久刪除這筆資料嗎？刪除後無法進行復原！',
    icon: 'warning',
    showCancelButton: true,
    customClass: {
      confirmButton: 'esun-green-btn',
      cancelButton: 'esun-cancel-btn'
    },
    confirmButtonText: '確定永久刪除',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const res = await api.delete(`/users/${userId}/hard`)
      if (res.success) {
        showSuccess('已永久刪除扣款帳戶')
        fetchAccounts()
      } else {
        showError(res.message)
      }
    } catch (error) {
      showError(error)
    }
  }
}

onMounted(() => {
  fetchAccounts()
})
</script>
