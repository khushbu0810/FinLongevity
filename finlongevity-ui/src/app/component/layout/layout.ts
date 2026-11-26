import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-layout',
  styleUrl: './layout.css',
  templateUrl: './layout.html',
})
export class Layout {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  get initials(): string {
    return (this.auth.currentUser()?.fullName || 'FL').split(' ').map(x => x[0]).slice(0, 2).join('').toUpperCase();
  }
  logout(): void { this.auth.logout(); this.router.navigate(['/login']); }
}
