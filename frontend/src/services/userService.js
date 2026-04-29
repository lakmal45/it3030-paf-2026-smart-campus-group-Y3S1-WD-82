import api from "./api";

const BASE_PATH = "/user";

export const getMe = async () => {
    try {
        const response = await api.get(`${BASE_PATH}/me`);
        return response.data;
    } catch (error) {
        throw error.response?.data || "Failed to fetch user data";
    }
};

export const updateProfile = async (updateData) => {
    try {
        const response = await api.put(`${BASE_PATH}/me`, updateData);
        return response.data;
    } catch (error) {
        throw error.response?.data || "Failed to update profile";
    }
};

export const changePassword = async (oldPassword, newPassword) => {
    try {
        const response = await api.put(`${BASE_PATH}/me/password`, { oldPassword, newPassword });
        return response.data;
    } catch (error) {
        throw error.response?.data || "Failed to change password";
    }
};

export const deleteAccount = async () => {
    try {
        const response = await api.delete(`${BASE_PATH}/me`);
        return response.data;
    } catch (error) {
        throw error.response?.data || "Failed to delete account";
    }
};

export const updateNotificationPrefs = async (prefs) => {
    try {
        const response = await api.put(`${BASE_PATH}/me/notifications`, prefs);
        return response.data;
    } catch (error) {
        throw error.response?.data || "Failed to update notification preferences";
    }
};
