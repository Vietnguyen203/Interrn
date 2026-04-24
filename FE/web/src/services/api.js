const API_BASE_URL = '/api';

// Helper to get auth headers
const getAuthHeaders = () => {
    const token = sessionStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

// Safe JSON parser - handles empty body
const safeJson = async (response) => {
    const text = await response.text();
    if (!text || text.trim() === '') return {};
    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
};

export const apiService = {
    get: async (endpoint) => {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'GET',
            headers: getAuthHeaders(),
        });
        const data = await safeJson(response);
        if (!response.ok || data.code === 'ERROR') {
            throw new Error(data.message || `API error: ${response.status}`);
        }
        return data;
    },

    post: async (endpoint, payload) => {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload),
        });
        const data = await safeJson(response);
        if (!response.ok || data.code === 'ERROR') {
            throw new Error(data.message || `API error: ${response.status}`);
        }
        return data;
    },

    put: async (endpoint, payload) => {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload),
        });
        const data = await safeJson(response);
        if (!response.ok || data.code === 'ERROR') {
            throw new Error(data.message || `API error: ${response.status}`);
        }
        return data;
    },

    delete: async (endpoint) => {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });
        const data = await safeJson(response);
        if (!response.ok || data.code === 'ERROR') {
            throw new Error(data.message || `API error: ${response.status}`);
        }
        return data;
    },

    // Specific API calls for cleaner components
    auth: {
        login: (username, password) =>
            apiService.post('/users-service/request/login', { username, password }),
        resetPassword: (id, password) =>
            apiService.put(`/users-service/request/${id}/reset-password`, { password })
    },

    dashboard: {
        getTables: () => apiService.get('/tables/list'),
        getRecentOrders: () => apiService.get('/orders?page=0&size=5'),
        getRevenue: () => apiService.get('/orders/revenue-by-week'),
        getAllOrders: (page = 0, size = 50) => apiService.get(`/orders?page=${page}&size=${size}`),

        getFoods: () => apiService.get('/foods/list'),
        createFood: (foodData) => apiService.post('/foods/create', foodData),
        updateFood: (id, foodData) => apiService.put(`/foods/${id}`, foodData),
        deleteFood: (id) => apiService.delete(`/foods/${id}`),

        getStaff: (server = 'HCM') => apiService.get(`/users?server=${server}`),
        createStaff: (staffData) => apiService.post('/users/register', staffData),
        updateStaff: (server, employeeId, staffData) => apiService.put(`/users/${server}/${employeeId}`, staffData),
        deleteStaff: (server, employeeId) => apiService.delete(`/users/${server}/${employeeId}`),
    },

    kitchen: {
        getPendingItems: () => apiService.get('/kitchen/items'),
        updateItemStatus: (orderItemId, status) => apiService.put(`/kitchen/items/${orderItemId}/status`, { status })
    }
};
