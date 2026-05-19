import { APP_INITIALIZER, ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { authHttpInterceptor } from './adapter/auth/auth-http.interceptor';
import { ThemeService, initThemeFromStorage } from './application/theme/theme.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideAnimations(),
    provideHttpClient(withInterceptors([authHttpInterceptor])),
    provideRouter(routes),
    {
      provide: APP_INITIALIZER,
      useFactory: (themeService: ThemeService) => () => {
        initThemeFromStorage();
        themeService.initFromStorage();
      },
      deps: [ThemeService],
      multi: true,
    },
  ],
};
