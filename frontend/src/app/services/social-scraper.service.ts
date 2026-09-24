import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface SocialScrapeResult {
  platform: string;
  handle: string;
  displayName: string;
  profileUrl: string;
  bio: string;
  stats: Record<string, unknown>;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class SocialScraperService {
  constructor(private readonly http: HttpClient) {}

  scrape(platform: string, url: string): Observable<SocialScrapeResult> {
    return this.http.post<SocialScrapeResult>('http://localhost:8081/api/passport/scrape-social', { platform, url });
  }
}
