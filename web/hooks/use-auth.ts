import {useMutation} from "@tanstack/react-query";
import {useRouter} from "next/navigation";
import {useAuthStore} from "@/store/auth";
import api from "@/lib/api";
import {ApiErrorResponse, AuthRequest, AuthResponse, DecodedToken, RegisterRequest} from "@/types";
import {toast} from "sonner";
import {AxiosError} from "axios";
import {getErrorMessage} from "@/lib/utils";
import {jwtDecode} from "jwt-decode";

export const useAuth = () => {
    const {setToken, clearToken, isAuthenticated, userEmail, forcePasswordChange} = useAuthStore();
    const router = useRouter();

    const loginMutation = useMutation<
        AuthResponse,
        AxiosError<ApiErrorResponse>,
        AuthRequest
    >({
        mutationFn: (credentials) =>
            api.post("/api/v1/auth/login", credentials).then((res) => res.data),
        onSuccess: (data) => {
            try {
                const decoded: DecodedToken = jwtDecode(data.accessToken);
                setToken(data.accessToken, decoded.sub, decoded.forcePasswordChange);

                const isSteward = decoded.organizationId === undefined;
                const isOrganizerOrScanner = decoded.organizationId !== undefined;

                if (decoded.forcePasswordChange) {
                    toast.info("Password change required", {
                        description: "Please create a new password to continue.",
                    });
                    router.replace("/force-password-change");
                } else if (isSteward) {
                    toast.success("Login successful!", {
                        description: "Welcome back, Admin.",
                    });
                    router.replace("/admin/dashboard");
                } else if (isOrganizerOrScanner) {
                    toast.success("Login successful!", {
                        description: "Redirecting to your dashboard...",
                    });
                    router.replace("/dashboard");
                } else {
                    toast.error("Access Denied", {
                        description: "This account does not have permission to access the dashboard.",
                    });
                    clearToken();
                }
            } catch (e) {
                console.error("Failed to process token on login success:", e);
                toast.error("Login failed", {description: "An issue occurred after login. Please try again."});
                clearToken();
            }
        },
        onError: (error) => {
            const defaultMessage = "Invalid email or password.";
            const errorMessage = getErrorMessage(error, defaultMessage) || defaultMessage;

            if (errorMessage.includes("Account is not verified")) {
                toast.warning("Account Not Verified", {
                    description: "Please check your inbox for a verification email to activate your account.",
                });
            } else if (errorMessage.includes("not active") || errorMessage.includes("subscription has expired")) {
                toast.error("Account Access Issue", {
                    description: errorMessage,
                });
            } else {
                toast.error("Login Failed", {
                    description: errorMessage,
                });
            }
        },
    });

    const registerMutation = useMutation<
        void,
        AxiosError<ApiErrorResponse>,
        RegisterRequest
    >({
        mutationFn: (userInfo) =>
            api.post("/api/v1/organizations", userInfo).then((res) => res.data),
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
        userEmail,
        forcePasswordChange,
    };
};
