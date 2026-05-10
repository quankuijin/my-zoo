import api from './index'

export const getPermissions = (params) => api.get('/permissions', { params })

export const getAllPermissions = () => api.get('/permissions/all')

export const getPermission = (id) => api.get(`/permissions/${id}`)

export const createPermission = (data) => api.post('/permissions', data)

export const updatePermission = (id, data) => api.put(`/permissions/${id}`, data)

export const deletePermission = (id) => api.delete(`/permissions/${id}`)
