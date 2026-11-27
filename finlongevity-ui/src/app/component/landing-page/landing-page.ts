import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';


@Component({
  standalone: true,
  imports: [RouterLink],
  selector: 'app-landing-page',
  styleUrl: './landing-page.css',
  templateUrl: './landing-page.html',
})
export class LandingPage {
  readonly features = [
    {
      icon: '↗',
      title: 'Income & Employment',
      description: 'Track your income sources, employment details and financial stability.'
    },
    {
      icon: '⌁',
      title: 'Expense Management',
      description: 'Understand your spending habits and build better financial discipline.'
    },
    {
      icon: '◇',
      title: 'Assets & Liabilities',
      description: 'Manage your assets, liabilities and monitor your overall net worth.'
    },
    {
      icon: '◈',
      title: 'Investment Planning',
      description: 'Explore investment opportunities based on your financial goals.'
    },
    {
      icon: '◎',
      title: 'Risk Assessment',
      description: 'Understand your risk tolerance and investment preferences.'
    },
    {
      icon: '⌂',
      title: 'Household Planning',
      description: 'Organize family finances, household members and nominees.'
    }
  ];

  readonly steps = [
    {
      number: '01',
      title: 'Create your account',
      description: 'Sign up securely and start your financial journey.'
    },
    {
      number: '02',
      title: 'Build your financial picture',
      description: 'Add your income, expenses, assets and liabilities.'
    },
    {
      number: '03',
      title: 'Understand your finances',
      description: 'Get a clearer picture of your financial position.'
    },
    {
      number: '04',
      title: 'Plan with confidence',
      description: 'Make informed decisions aligned with your goals.'
    }
  ];

  scrollTo(section: string): void {
    document.getElementById(section)?.scrollIntoView({
      behavior: 'smooth'
    });
  }
}








