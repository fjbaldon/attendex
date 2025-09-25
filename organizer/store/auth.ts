import {create} from "zustand";
import {createJSONStorage, persist} from "zustand/middleware";

interface AuthState {
    accessToken: string | null;
    isAuthenticated: boolean;
    userEmail: string | null;
    setToken: (token: string, email: string) => void;
    clearToken: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            accessToken: null,
            isAuthenticated: false,
            userEmail: null,
            setToken: (token, email) =>
                set({
                    accessToken: token,
                    isAuthenticated: true,
                    userEmail: email,
                }),
            clearToken: () =>
                set({
                    accessToken: null,
                    isAuthenticated: false,
                    userEmail: null,
                }),
        }),
        {
            name: "auth-storage",
            storage: createJSONStorage(() => localStorage),
        }
    )
);
