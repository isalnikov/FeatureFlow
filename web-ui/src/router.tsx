import { lazy, Suspense } from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { Loading } from './components/common/Loading';

const DashboardPage = lazy(async () => {
  const module = await import('./pages/DashboardPage');
  return { default: module.DashboardPage };
});

const PortfolioPage = lazy(async () => {
  const module = await import('./pages/PortfolioPage');
  return { default: module.PortfolioPage };
});

const PlanPage = lazy(async () => {
  const module = await import('./pages/PlanPage');
  return { default: module.PlanPage };
});

const TeamsPage = lazy(async () => {
  const module = await import('./pages/TeamsPage');
  return { default: module.TeamsPage };
});

const FeaturesPage = lazy(async () => {
  const module = await import('./pages/FeaturesPage');
  return { default: module.FeaturesPage };
});

const SimulationPage = lazy(async () => {
  const module = await import('./pages/SimulationPage');
  return { default: module.SimulationPage };
});

const SettingsPage = lazy(async () => {
  const module = await import('./pages/SettingsPage');
  return { default: module.SettingsPage };
});

const withSuspense = (element: React.ReactNode) => (
  <Suspense fallback={<Loading fullScreen label="Loading..." />}>{element}</Suspense>
);

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: withSuspense(<DashboardPage />) },
      { path: 'portfolio', element: withSuspense(<PortfolioPage />) },
      { path: 'plan', element: withSuspense(<PlanPage />) },
      { path: 'teams', element: withSuspense(<TeamsPage />) },
      { path: 'features', element: withSuspense(<FeaturesPage />) },
      { path: 'simulation', element: withSuspense(<SimulationPage />) },
      { path: 'settings', element: withSuspense(<SettingsPage />) },
    ],
  },
]);
