import {useMutation} from "@tanstack/react-query";
import {useRouter} from "next/navigation";
import {useAuthStore} from "@/store/auth";
import api from "@/lib/api";
import {ApiErrorResponse, AuthRequest, AuthResponse, DecodedToken, RegisterRequest} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {jwtDecode} from "jwt-decode";
import {getErrorMessage} from "@/lib/utils";

export const useAuth = () => {
    const {setToken, clearToken, isAuthenticated} = useAuthStore();
    const router = useRouter();

    const loginMutation = useMutation<
        AuthResponse,
        AxiosError<ApiErrorResponse>,
        AuthRequest
    >({
        mutationFn: (credentials) =>
            api.post("/api/v1/auth/login", credentials).then((res) => res.data),
        onSuccess: (data) => {
            const decodedToken: DecodedToken = jwtDecode(data.accessToken);
            const userRoles = decodedToken.roles || [];
            const isOrganizer = userRoles.includes('ROLE_ORGANIZER');

            if (!isOrganizer) {
                toast.error("Access Denied", {
                    description: "This account does not have permission to access the organizer dashboard.",
                });
                return;
            }

            setToken(data.accessToken, decodedToken.sub, decodedToken.forcePasswordChange);

            if (decodedToken.forcePasswordChange) {
                toast.info("Password change required", {
                    description: "Please create a new password to continue.",
                });
                router.replace("/force-password-change");
            } else {
                toast.success("Login successful!", {
                    description: "Redirecting to your dashboard...",
                });
                router.replace("/dashboard");
            }
        },
        onError: (error) => {
            const errorMessage = getErrorMessage(error, "Invalid email or password.");
            toast.error("Login failed", {
                description: errorMessage,
            });
        },
    });

    const registerMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        RegisterRequest
    >({
        mutationFn: (userInfo) =>
            api.post("/api/v1/auth/register-organization", userInfo).then((res) => res.data),
        onSuccess: () => {
            router.push("/register-success");
        },
        onError: (error) => {
            const errorMessage = getErrorMessage(error, "An unknown error occurred.");
            toast.error("Registration failed", {
                description: errorMessage,
            });
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
        register: registerMutation.mutate,
        isRegistering: registerMutation.isPending,
        logout,
        isAuthenticated,
    };
};
