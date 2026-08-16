import { Routes } from '@angular/router';
import { Homepage } from './homepage/homepage';
import { MyTasks } from './my-tasks/my-tasks';
import { Search } from './search/search';
import { LoginComponent } from './login-component/login-component';
import { LoggedInGuardService } from '../services/logged-in-guard.service';
import { UsersPanel } from './users-panel/users-panel';
import { AdminGuard } from '../services/admin-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'my-tasks' },
  { path: 'home', component: Homepage },
  { path: 'my-tasks', component: MyTasks, canActivate: [LoggedInGuardService] },
  { path: 'search', component: Search, canActivate: [LoggedInGuardService] },
  { path: 'login', component: LoginComponent },
  { path: 'users', component: UsersPanel, canActivate: [AdminGuard] }
];
