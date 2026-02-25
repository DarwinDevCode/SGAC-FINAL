import { ApplicationConfig, provideZoneChangeDetection, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';

import {
  LucideAngularModule,
  Eye,
  EyeOff,
  LayoutDashboard,
  FileText,
  FolderOpen,
  Award,
  Bell,
  CalendarClock,
  Users,
  CheckSquare,
  BarChart3,
  FileSignature,
  Settings,
  LogOut,
  Menu,
  Search,
  Plus,
  Power,
  Edit2,
  X,
  Shield,
  ShieldCheck,
  Database, Trash2, PlusCircle, Folder, ChevronRight, ChevronDown, Info, CheckCircle, AlertCircle
} from 'lucide-angular';
import {authInterceptor} from './core/interceptors/auth-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    importProvidersFrom(
      LucideAngularModule.pick({
        Eye,
        EyeOff,
        LayoutDashboard,
        FileText,
        FolderOpen,
        Award,
        Bell,
        CalendarClock,
        Users,
        CheckSquare,
        BarChart3,
        FileSignature,
        Settings,
        LogOut,
        Menu,
        Search,
        Plus,
        Power,
        Edit2,
        X,
        Shield,
        ShieldCheck,
        Database,
        Trash2,
        PlusCircle,
        Folder,
        ChevronRight,
        ChevronDown,
        Info,
        CheckCircle,
        AlertCircle
      })
    )
  ]
};
