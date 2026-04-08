import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response && error.response.data) {
      return Promise.reject(error.response.data.message || 'Error occurred');
    }
    return Promise.reject('Network Error');
  }
)

export default apiClient
