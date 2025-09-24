import {create} from "zustand";
import {createJSONStorage, persist} from "zustand/middleware";

interface AuthState {
    accessToken: string | null;
    isAuthenticated: boolean;
    setToken: (token: string) => void;
    clearToken: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            accessToken: null,
            isAuthenticated: false,
            setToken: (token) =>
                set({
                    accessToken: token,
                    isAuthenticated: true,
                }),
            clearToken: () =>
                set({
                    accessToken: null,
                    isAuthenticated: false,
                }),
        }),
        {
            name: "auth-storage",
            storage: createJSONStorage(() => localStorage),
        }
    )
);
