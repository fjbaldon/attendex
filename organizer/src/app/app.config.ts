import {ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';
import {authInterceptor} from './core/interceptors/auth-interceptor';

import {routes} from './app.routes';

import {provideHttpClient, withInterceptors} from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
