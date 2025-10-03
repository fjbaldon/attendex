import {create} from "zustand";
import {createJSONStorage, persist} from "zustand/middleware";

interface AuthState {
    accessToken: string | null;
    isAuthenticated: boolean;
    userEmail: string | null;
    forcePasswordChange: boolean;
    setToken: (token: string, email: string, forcePasswordChange: boolean) => void;
    clearToken: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            accessToken: null,
            isAuthenticated: false,
            userEmail: null,
            forcePasswordChange: false,
            setToken: (token, email, forcePasswordChange) =>
                set({
                    accessToken: token,
                    isAuthenticated: true,
                    userEmail: email,
                    forcePasswordChange: forcePasswordChange,
                }),
            clearToken: () =>
                set({
                    accessToken: null,
                    isAuthenticated: false,
                    userEmail: null,
                    forcePasswordChange: false,
                }),
        }),
        {
            name: "auth-storage",
            storage: createJSONStorage(() => localStorage),
        }
    )
);
