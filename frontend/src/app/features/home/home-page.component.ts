import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { SocialScraperService, SocialScrapeResult } from '../../services/social-scraper.service';

interface RegistrationResponse {
  email: string;
  status: string;
  verificationCode: string;
  message: string;
}

interface PassportSummary {
  studentId: string;
  email: string;
  displayName: string;
  status: string;
  campus: string;
  major: string;
  interests: string[];
  connectionCount: number;
  communityCount: number;
  lastUpdated: string;
}

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule],
  template: `
    <div class="topbar">
      <div class="brand">Campus Passport</div>
      <nav class="nav">
        <a class="active" href="#">Home</a>
        <a href="#">Communities</a>
        <a href="#">Projects</a>
        <a href="#">Messaging</a>
      </nav>
    </div>

    <main class="page">
      <section class="hero">
        <div class="card hero-card">
          <h1>Verified campus life, in one passport.</h1>
          <p>
            Discover communities, collaborate on projects, and build a trusted student identity using
            only campus-verified data and privacy-first controls.
          </p>
          <div class="action-row">
            <button class="primary-btn" type="button">Join campus network</button>
            <button class="secondary-btn" type="button">Explore communities</button>
          </div>
        </div>

        <div class="card form-card">
          <h2>Campus email verification</h2>
          <form [formGroup]="registerForm" (ngSubmit)="register()" class="form-grid">
            <label>
              First name
              <input formControlName="firstName" placeholder="Avery" />
            </label>
            <label>
              Last name
              <input formControlName="lastName" placeholder="Chen" />
            </label>
            <label>
              Campus email
              <input formControlName="email" placeholder="student@campus.edu" />
            </label>
            <label>
              Password
              <input type="password" formControlName="password" placeholder="Minimum 8 characters" />
            </label>
            <button class="primary-btn" type="submit" [disabled]="registerForm.invalid || submitting">
              {{ submitting ? 'Sending...' : 'Register' }}
            </button>
          </form>

          <div *ngIf="registerMessage" class="form-note">{{ registerMessage }}</div>
          <div *ngIf="errorMessage" class="error-box">{{ errorMessage }}</div>

          <div *ngIf="verificationCode" class="form-note" style="margin-top: 1rem;">
            Demo verification code: <strong>{{ verificationCode }}</strong>
          </div>
        </div>
      </section>

      <section class="hero">
        <div class="card passport-card">
          <div class="badge">Verified Student</div>
          <h2>Passport summary</h2>
          <div *ngIf="passportSummary; else emptyState">
            <h3>{{ passportSummary.displayName }}</h3>
            <p>{{ passportSummary.email }}</p>
            <div class="meta-list">
              <div><span>Campus</span><strong>{{ passportSummary.campus }}</strong></div>
              <div><span>Major</span><strong>{{ passportSummary.major }}</strong></div>
              <div><span>Connections</span><strong>{{ passportSummary.connectionCount }}</strong></div>
              <div><span>Communities</span><strong>{{ passportSummary.communityCount }}</strong></div>
            </div>
          </div>
          <ng-template #emptyState>
            <p class="form-note">Complete verification to reveal the passport summary.</p>
          </ng-template>
        </div>

        <div class="card form-card">
          <h2>Verify email</h2>
          <form [formGroup]="verifyForm" (ngSubmit)="verifyEmail()" class="form-grid">
            <label>
              Email
              <input formControlName="email" placeholder="student@campus.edu" />
            </label>
            <label>
              Code
              <input formControlName="code" placeholder="123456" />
            </label>
            <button class="secondary-btn" type="submit" [disabled]="verifyForm.invalid || verifying">
              {{ verifying ? 'Verifying...' : 'Verify email' }}
            </button>
          </form>

          <div *ngIf="verificationSuccess" class="form-note" style="color: var(--success); margin-top: 1rem;">
            {{ verificationSuccess }}
          </div>
        </div>
      </section>

      <section class="hero">
        <div class="card form-card">
          <h2>Public profile ingestion</h2>
          <form [formGroup]="scrapeForm" (ngSubmit)="scrapeProfile()" class="form-grid">
            <label>
              Platform
              <select formControlName="platform">
                <option value="leetcode">LeetCode</option>
                <option value="github">GitHub</option>
                <option value="linkedin">LinkedIn</option>
                <option value="codeforces">Codeforces</option>
              </select>
            </label>
            <label>
              Profile URL or handle
              <input formControlName="url" placeholder="leetcode.com/yourname" />
            </label>
            <button class="primary-btn" type="submit" [disabled]="scrapeForm.invalid || scraping">
              {{ scraping ? 'Scraping...' : 'Scrape profile' }}
            </button>
          </form>

          <div *ngIf="scrapeError" class="error-box">{{ scrapeError }}</div>

          <div *ngIf="scrapeResult" style="margin-top: 1rem;">
            <div class="badge">{{ scrapeResult.platform }}</div>
            <h3>{{ scrapeResult.displayName }}</h3>
            <p><strong>{{ '@' + scrapeResult.handle }}</strong></p>
            <p>{{ scrapeResult.bio }}</p>
            <div class="meta-list">
              <div><span>Profile</span><strong>{{ scrapeResult.profileUrl }}</strong></div>
              <div *ngFor="let item of objectEntries(scrapeResult.stats)">
                <span>{{ item[0] }}</span>
                <strong>{{ item[1] }}</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="card stat-card">
          <strong>Scrape-ready</strong>
          <span>Supports LeetCode, GitHub, LinkedIn, and Codeforces public profile parsing.</span>
        </div>
      </section>

      <section class="stats-grid">
        <div class="card stat-card">
          <strong>12k+</strong>
          <span>verified campus members</span>
        </div>
        <div class="card stat-card">
          <strong>320</strong>
          <span>active communities</span>
        </div>
        <div class="card stat-card">
          <strong>95%</strong>
          <span>project collaboration success</span>
        </div>
        <div class="card stat-card">
          <strong>24/7</strong>
          <span>campus notifications</span>
        </div>
      </section>
    </main>
  `
})
export class HomePageComponent {
  registerForm!: FormGroup;
  verifyForm!: FormGroup;
  scrapeForm!: FormGroup;

  submitting = false;
  verifying = false;
  scraping = false;
  registerMessage = '';
  verificationCode = '';
  errorMessage = '';
  passportSummary: PassportSummary | null = null;
  verificationSuccess = '';
  scrapeResult: SocialScrapeResult | null = null;
  scrapeError = '';

  constructor(
    private readonly http: HttpClient,
    private readonly fb: FormBuilder,
    private readonly socialScraperService: SocialScraperService
  ) {
    this.registerForm = this.fb.nonNullable.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });

    this.verifyForm = this.fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      code: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.scrapeForm = this.fb.nonNullable.group({
      platform: ['leetcode', Validators.required],
      url: ['', Validators.required]
    });
  }

  register(): void {
    this.errorMessage = '';
    this.registerMessage = '';
    this.verificationCode = '';

    if (this.registerForm.invalid) {
      this.errorMessage = 'Please complete all required fields with a valid campus email.';
      return;
    }

    this.submitting = true;
    this.http.post<RegistrationResponse>('http://localhost:8081/api/auth/register', this.registerForm.getRawValue())
      .subscribe({
        next: (response) => {
          this.submitting = false;
          this.registerMessage = response.message || 'Registration received.';
          this.verificationCode = response.verificationCode || '';
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err?.error?.message || 'Unable to register. Please double-check your campus email.';
        }
      });
  }

  verifyEmail(): void {
    this.errorMessage = '';
    this.verificationSuccess = '';

    if (this.verifyForm.invalid) {
      this.errorMessage = 'Enter a valid campus email and verification code.';
      return;
    }

    this.verifying = true;
    const payload = this.verifyForm.getRawValue();
    this.http.post<{ status: string; message: string; userId: string; email: string }>('http://localhost:8081/api/auth/verify-email', payload)
      .subscribe({
        next: (response) => {
          this.verifying = false;
          this.verificationSuccess = response.message || 'Email verified.';
          this.loadPassport(payload.email);
        },
        error: (err) => {
          this.verifying = false;
          this.errorMessage = err?.error?.message || 'Verification failed.';
        }
      });
  }

  private loadPassport(email: string): void {
    this.http.get<PassportSummary>('http://localhost:8081/api/passport/me', {
      headers: { 'X-User-Email': email }
    }).subscribe({
      next: (summary) => {
        this.passportSummary = summary;
      },
      error: () => {
        this.passportSummary = null;
      }
    });
  }

  scrapeProfile(): void {
    this.scrapeError = '';
    if (this.scrapeForm.invalid) {
      this.scrapeError = 'Choose a platform and provide a valid URL or handle.';
      return;
    }

    this.scraping = true;
    const { platform, url } = this.scrapeForm.getRawValue();

    this.socialScraperService.scrape(platform, url).subscribe({
      next: (result) => {
        this.scraping = false;
        this.scrapeResult = result;
      },
      error: (err) => {
        this.scraping = false;
        this.scrapeError = err?.error?.message || 'Unable to scrape this public profile.';
      }
    });
  }

  objectEntries(value: Record<string, unknown> | undefined): Array<[string, unknown]> {
    return value ? Object.entries(value) : [];
  }
}
