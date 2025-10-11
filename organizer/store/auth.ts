import {create} from "zustand";
import {createJSONStorage, persist} from "zustand/middleware";
import Cookies from 'js-cookie';

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
            setToken: (token, email, forcePasswordChange) => {
                Cookies.set('auth-token', token, {expires: 1, path: '/'});
                set({
                    accessToken: token,
                    isAuthenticated: true,
                    userEmail: email,
                    forcePasswordChange: forcePasswordChange,
                });
            },
            clearToken: () => {
                Cookies.remove('auth-token', {path: '/'});
                set({
                    accessToken: null,
                    isAuthenticated: false,
                    userEmail: null,
                    forcePasswordChange: false,
                });
            },
        }),
        {
            name: "auth-storage",
            storage: createJSONStorage(() => localStorage),
        }
    )
);
