import {NextRequest, NextResponse} from 'next/server';
import {jwtDecode} from 'jwt-decode';

interface DecodedToken {
    forcePasswordChange: boolean;
    exp: number;
    roles: string[];
}

export function middleware(request: NextRequest) {
    const publicPaths = ['/login', '/register', '/verify', '/register-success'];
    const token = request.cookies.get('auth-token')?.value;

    let isAuthenticated = false;
    let forcePasswordChange = false;
    let isTokenExpired = true;
    let roles: string[] = [];

    if (token) {
        try {
            const decoded: DecodedToken = jwtDecode(token);
            const currentTime = Date.now() / 1000;
            if (decoded.exp > currentTime) {
                isAuthenticated = true;
                isTokenExpired = false;
                forcePasswordChange = decoded.forcePasswordChange;
                roles = decoded.roles || [];
            }
        } catch (error) {
            console.error("Invalid token:", error);
        }
    }

    const {pathname} = request.nextUrl;
    const isSteward = roles.includes('ROLE_STEWARD');

    if (isAuthenticated) {
        if (forcePasswordChange && pathname !== '/force-password-change') {
            return NextResponse.redirect(new URL('/force-password-change', request.url));
        }

        if (isSteward) {
            if (!pathname.startsWith('/admin') && pathname !== '/force-password-change') {
                return NextResponse.redirect(new URL('/admin/dashboard', request.url));
            }
        } else { // Assumes ROLE_ORGANIZER
            if (pathname.startsWith('/admin')) {
                return NextResponse.redirect(new URL('/dashboard', request.url));
            }
            if (publicPaths.includes(pathname)) {
                return NextResponse.redirect(new URL('/dashboard', request.url));
            }
        }
    } else {
        const isProtectedRoute = !publicPaths.includes(pathname) && pathname !== '/force-password-change';
        if (isProtectedRoute) {
            const redirectUrl = new URL('/login', request.url);
            if (isTokenExpired && token) {
                redirectUrl.searchParams.set('sessionExpired', 'true');
            }
            return NextResponse.redirect(redirectUrl);
        }
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        '/dashboard/:path*',
        '/admin/:path*',
        '/login',
        '/register',
        '/force-password-change',
        '/verify',
        '/register-success',
    ],
};
