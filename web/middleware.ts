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
    const {pathname} = request.nextUrl;

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

    if (!isAuthenticated) {
        if (publicPaths.includes(pathname)) {
            return NextResponse.next();
        }

        const redirectUrl = new URL('/login', request.url);
        if (isTokenExpired && token) {
            redirectUrl.searchParams.set('sessionExpired', 'true');
        }
        return NextResponse.redirect(redirectUrl);
    }

    if (forcePasswordChange) {
        if (pathname !== '/force-password-change') {
            return NextResponse.redirect(new URL('/force-password-change', request.url));
        }
        return NextResponse.next(); // Allow access to change password page
    }

    if (pathname === '/force-password-change') {
        const home = roles.includes('ROLE_STEWARD') ? '/admin/dashboard' : '/dashboard';
        return NextResponse.redirect(new URL(home, request.url));
    }

    if (publicPaths.includes(pathname)) {
        const home = roles.includes('ROLE_STEWARD') ? '/admin/dashboard' : '/dashboard';
        return NextResponse.redirect(new URL(home, request.url));
    }

    const isSteward = roles.includes('ROLE_STEWARD');

    if (isSteward) {
        if (!pathname.startsWith('/admin')) {
            return NextResponse.redirect(new URL('/admin/dashboard', request.url));
        }
    } else {
        if (pathname.startsWith('/admin')) {
            return NextResponse.redirect(new URL('/dashboard', request.url));
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
