import { HttpContextToken } from '@angular/common/http';
export const SKIP_AUTH_ERROR = new HttpContextToken<boolean>(() => false);