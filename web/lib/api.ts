import axios from "axios";
import {useAuthStore} from "@/store/auth";

const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use(
    (config) => {
        const token = useAuthStore.getState().accessToken;
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (axios.isAxiosError(error) && error.response) {
            const originalRequest = error.config;
            if (originalRequest && originalRequest.url === "/api/v1/auth/login") {
                return Promise.reject(error);
            }

            if (error.response.status === 401) {
                const {clearToken, accessToken} = useAuthStore.getState();

                if (accessToken) {
                    clearToken();
                    window.location.href = '/login?sessionExpired=true';
                }
            }
        }
        return Promise.reject(error);
    }
);

export default api;
