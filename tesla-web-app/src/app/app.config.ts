import { ApplicationConfig, inject, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { InMemoryCache } from '@apollo/client/core';
import { HttpLink } from 'apollo-angular/http';
import { provideApollo } from 'apollo-angular';
import { routes } from './app.routes';
import { API_BASE_URL } from './core/http/api-url.token';
import { authInterceptor } from './core/http/auth.interceptor';
import { authErrorInterceptor } from './core/http/auth-error.interceptor';
import { environment } from '../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor, authErrorInterceptor])),
    { provide: API_BASE_URL, useValue: environment.apiBaseUrl },
    provideApollo(() => {
      const httpLink = inject(HttpLink);
      return {
        link: httpLink.create({ uri: `${environment.apiBaseUrl}/graphql` }),
        cache: new InMemoryCache(),
      };
    }),
  ],
};
