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

    // ===== KITCHEN (dùng order-service port 8082) =====
    kitchen: {
        // Lấy tất cả đơn PENDING / CONFIRMED để bếp xử lý
        getPendingOrders: () => orderFetch('GET', '/orders?status=PENDING'),
        getConfirmedOrders: () => orderFetch('GET', '/orders?status=CONFIRMED'),

        // Cập nhật trạng thái chế biến của 1 món
        // status: PENDING | COOKING | READY | SERVED
        updateItemStatus: (itemId, status) =>
            orderFetch('PATCH', `/orders/items/${itemId}/kitchen-status?status=${status}`),
    },

    // ===== CATALOG SERVICE (port 8081 via /catalog proxy) =====
    catalog: {
        // Categories
        getCategories: () => catalogFetch('GET', '/catalog-service/categories'),
        createCategory: (data) => catalogFetch('POST', '/catalog-service/categories', data),
        updateCategory: (id, data) => catalogFetch('PUT', `/catalog-service/categories/${id}`, data),
        deleteCategory: (id) => catalogFetch('DELETE', `/catalog-service/categories/${id}`),

        // Menu Items
        getItems: () => catalogFetch('GET', '/catalog-service/items'),
        getItemsByCategory: (categoryId) => catalogFetch('GET', `/catalog-service/items/category/${categoryId}`),
        createItem: (data) => catalogFetch('POST', '/catalog-service/items', data),
        updateItem: (id, data) => catalogFetch('PUT', `/catalog-service/items/${id}`, data),
        deleteItem: (id) => catalogFetch('DELETE', `/catalog-service/items/${id}`),
    },

    // ===== ORDER SERVICE (port 8082 via /order proxy) =====
    order: {
        // Lấy tất cả đơn / lọc theo status
        getAll: (status) => orderFetch('GET', `/orders${status ? `?status=${status}` : ''}`),
        getById: (id) => orderFetch('GET', `/orders/${id}`),

        // Tạo đơn hàng mới
        create: (data) => orderFetch('POST', '/orders', data),

        // Thêm / sửa / xóa món trong đơn
        addItem: (orderId, item) => orderFetch('POST', `/orders/${orderId}/items`, item),
        updateItem: (orderId, itemId, data) => orderFetch('PUT', `/orders/${orderId}/items/${itemId}`, data),
        removeItem: (orderId, itemId) => orderFetch('DELETE', `/orders/${orderId}/items/${itemId}`),

        // Đổi trạng thái đơn: PENDING | CONFIRMED | COMPLETED | CANCELLED
        updateStatus: (orderId, status) => orderFetch('PATCH', `/orders/${orderId}/status?status=${status}`),

        // Hủy đơn
        cancel: (orderId) => orderFetch('DELETE', `/orders/${orderId}`),

        // Bếp cập nhật trạng thái chế biến: PENDING | COOKING | READY | SERVED
        updateKitchenStatus: (itemId, status) =>
            orderFetch('PATCH', `/orders/items/${itemId}/kitchen-status?status=${status}`),
    }
};

// Helper riêng cho catalog-service (proxy /catalog → port 8081)
async function catalogFetch(method, path, body) {
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
    const response = await fetch(`/catalog${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });
    const text = await response.text();
    const data = text ? JSON.parse(text) : {};
    if (!response.ok) throw new Error(data.message || `Catalog API error: ${response.status}`);
    return data;
}

// Helper riêng cho order-service (proxy /order → port 8082)
async function orderFetch(method, path, body) {
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
    const response = await fetch(`/order${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });
    const text = await response.text();
    const data = text ? JSON.parse(text) : {};
    if (!response.ok) throw new Error(data.message || `Order API error: ${response.status}`);
    return data;
}
