<template>
  <v-container>
    <v-card class="mb-4">
      <v-card-title class="d-flex align-center">
        扣款帳戶管理
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="openDialog()">新增帳戶</v-btn>
      </v-card-title>
      
      <v-data-table
        :headers="headers"
        :items="accounts"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.deleteAction="{ item }">
          <v-btn icon variant="text" color="red" @click="deleteAccount(item.userId)">
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </template>
        <template v-slot:item.editAction="{ item }">
          <v-btn icon size="small" variant="text" color="#1eacad" @click="openDialog(item)">
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="500px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ isEdit ? '編輯帳戶' : '新增帳戶' }}</span>
        </v-card-title>

        <v-card-text>
          <v-form ref="form" v-model="valid" lazy-validation>
            <v-text-field
              v-model="editedItem.userId"
              label="使用者 ID"
              :rules="userIdRules"
              :disabled="isEdit"
              required
            ></v-text-field>
            <v-text-field
              v-model="editedItem.userName"
              label="姓名"
              :rules="[v => !!v || '請輸入姓名']"
              required
            ></v-text-field>
            <v-text-field
              v-model="editedItem.email"
              label="電子郵件"
              :rules="emailRules"
              required
            ></v-text-field>
            <v-text-field
              v-model="editedItem.account"
              label="扣款帳號"
              :rules="accountRules"
              required
            ></v-text-field>
          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="save" :disabled="!valid">儲存</v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="closeDialog">取消</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import api from '../utils/api'

const headers = [
  { title: '', key: 'deleteAction', sortable: false, width: '60px' },
  { title: '使用者 ID', key: 'userId' },
  { title: '姓名', key: 'userName' },
  { title: '電子郵件', key: 'email' },
  { title: '扣款帳號', key: 'account' },
  { title: '建立時間', key: 'createdAt', width: '180px' },
  { title: '', key: 'editAction', sortable: false, width: '60px' }
]

const accounts = ref([])
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const valid = ref(false)

const editedItem = ref({ userId: '', userName: '', email: '', account: '' })

const userIdRules = [
  v => !!v || '請輸入使用者 ID',
  v => /^[A-Z][0-9]{10}$/.test(v) || '使用者 ID 須為大寫英文開頭加 10 位數字',
  v => {
    if (isEdit.value) return true
    return !accounts.value.some(acc => acc.userId === v) || '此使用者 ID 已存在'
  }
]

const emailRules = [
  v => !!v || '請輸入電子郵件',
  v => /.+@.+\..+/.test(v) || '電子郵件格式不正確'
]

const accountRules = [
  v => !!v || '請輸入扣款帳號',
  v => /^\d{10}$/.test(v) || '扣款帳號須為剛好 10 位數字',
  v => {
    const isDuplicate = accounts.value.some(acc => acc.account === v && acc.userId !== editedItem.value.userId)
    return !isDuplicate || '此扣款帳號已被其他使用者綁定'
  }
]

const fetchAccounts = async () => {
  loading.value = true
  try {
    const res = await api.get('/users/active')
    if (res.success) {
      accounts.value = res.data
    }
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

const openDialog = (item = null) => {
  if (item) {
    isEdit.value = true
    editedItem.value = { ...item }
  } else {
    isEdit.value = false
    editedItem.value = { userId: '', userName: '', email: '', account: '' }
  }
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
}

const save = async () => {
  try {
    let res
    if (isEdit.value) {
      res = await api.put(`/users/${editedItem.value.userId}`, editedItem.value)
    } else {
      res = await api.post('/users', editedItem.value)
    }
    
    if (res.success) {
      showSuccess('儲存成功')
      closeDialog()
      fetchAccounts()
    } else {
      showError(res.message)
    }
  } catch (error) {
    showError(error)
  }
}

const deleteAccount = async (id) => {
  const result = await Swal.fire({
    title: '確認刪除',
    text: '確定要刪除此帳戶嗎？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#1eacad',
    cancelButtonColor: '#1eacad',
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const res = await api.delete(`/users/${id}`)
      if (res.success) {
        showSuccess('帳戶已刪除')
        fetchAccounts()
      } else {
        showError(res.message)
      }
    } catch (error) {
      showError(error)
    }
  }
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

const showError = (text) => {
  const errMsg = text.response?.data?.message || text.message || String(text)
  Swal.fire({
    icon: 'error',
    title: '錯誤',
    text: errMsg
  })
}

onMounted(() => {
  fetchAccounts()
})
</script>
