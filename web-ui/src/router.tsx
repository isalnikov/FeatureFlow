import { lazy, Suspense } from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { Loading } from './components/common/Loading';

const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const PortfolioPage = lazy(() => import('./pages/PortfolioPage'));
const PlanPage = lazy(() => import('./pages/PlanPage'));
const TeamsPage = lazy(() => import('./pages/TeamsPage'));
const FeaturesPage = lazy(() => import('./pages/FeaturesPage'));
const SimulationPage = lazy(() => import('./pages/SimulationPage'));
const SettingsPage = lazy(() => import('./pages/SettingsPage'));

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
