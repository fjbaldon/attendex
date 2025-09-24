import {useMutation} from "@tanstack/react-query";
import {useRouter} from "next/navigation";
import {useAuthStore} from "@/store/auth";
import api from "@/lib/api";
import {AuthRequest, AuthResponse, RegisterRequest} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";

type ApiErrorResponse = {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
    validationErrors?: Record<string, string>;
};

const getErrorMessage = (error: AxiosError, defaultMessage: string): string => {
    const errorData = error.response?.data as ApiErrorResponse;

    if (!errorData) {
        return defaultMessage;
    }

    if (errorData.validationErrors) {
        const messages = Object.values(errorData.validationErrors);
        if (messages.length > 0) {
            return messages.join('. ');
        }
    }

    if (errorData.message) {
        return errorData.message;
    }

    return defaultMessage;
};


export const useAuth = () => {
    const {setToken, clearToken, isAuthenticated} = useAuthStore();
    const router = useRouter();

    const loginMutation = useMutation<AuthResponse, AxiosError<ApiErrorResponse>, AuthRequest>({
        mutationFn: (credentials) =>
            api.post("/api/v1/auth/login", credentials).then((res) => res.data),
        onSuccess: (data) => {
            setToken(data.accessToken);
            toast.success("Login successful!", {
                description: "Redirecting to your dashboard...",
            });
            router.replace("/dashboard");
        },
        onError: (error) => {
            const errorMessage = getErrorMessage(error, "Invalid username or password.");
            toast.error("Login failed", {
                description: errorMessage,
            });
            console.error("Login failed:", error.response?.data || error.message);
        },
    });

    const registerMutation = useMutation<void, AxiosError<ApiErrorResponse>, RegisterRequest>({
        mutationFn: (userInfo) => api.post("/api/v1/auth/register", userInfo).then((res) => res.data),
        onSuccess: () => {
            toast.success("Account created successfully!", {
                description: "Please log in to continue.",
            });
            router.push("/login");
        },
        onError: (error) => {
            const errorMessage = getErrorMessage(error, "An unknown error occurred.");
            toast.error("Registration failed", {
                description: errorMessage,
            });
            console.error("Registration failed:", error.response?.data || error.message);
        },
    });

    const logout = () => {
        clearToken();
        router.push("/login");
        setTimeout(() => toast.info("You have been logged out."), 100);
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
