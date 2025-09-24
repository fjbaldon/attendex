import {useMutation} from "@tanstack/react-query";
import {useRouter} from "next/navigation";
import {useAuthStore} from "@/store/auth";
import api from "@/lib/api";
import {AuthRequest, AuthResponse, RegisterRequest} from "@/types";

export const useAuth = () => {
    const {setToken, clearToken, isAuthenticated} = useAuthStore();
    const router = useRouter();

    const loginMutation = useMutation<AuthResponse, Error, AuthRequest>({
        mutationFn: (credentials) =>
            api.post("/api/v1/auth/login", credentials),
        onSuccess: (data) => {
            setToken(data.accessToken);
            router.push("/dashboard");
        },
        onError: (error) => {
            console.error("Login failed:", error);
        },
    });

    const registerMutation = useMutation<void, Error, RegisterRequest>({
        mutationFn: (userInfo) => api.post("/api/v1/auth/register", userInfo),
        onSuccess: () => {
            router.push("/login");
        },
        onError: (error) => {
            console.error("Registration failed:", error);
        },
    });

    const logout = () => {
        clearToken();
        router.push("/login");
    };

    return {
        login: loginMutation.mutate,
        isLoggingIn: loginMutation.isPending,
        loginError: loginMutation.error,
        register: registerMutation.mutate,
        isRegistering: registerMutation.isPending,
        registerError: registerMutation.error,
        logout,
        isAuthenticated,
    };
};
