import { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { LotContext } from './context/LotContext';

import HomeView from './views/HomeView/HomeView';
import LoginView from './views/LoginView/LoginView';
import LogoutView from './views/LogoutView';
import RegisterView from './views/RegisterView/RegisterView';
import UserProfileView from './views/UserProfileView/UserProfileView';
import AdminView from './views/AdminView/AdminView';
import MainNav from './components/MainNav/MainNav';
import ProtectedRoute from './components/ProtectedRoute';
import ValetConsoleView from './views/ValetConsoleView/ValetConsoleView';
import Banner from './components/Banner/Banner';
import SideImages from './components/SideImages/SideImages';
import UnauthorizedView from './views/UnauthorizedView';
import layoutStyles from "./Layout/Layout.module.css";
import PatronPickupView from './views/PatronPickupView/PatronPickupView';

export default function App() {
  const [lotId, setLotId] = useState(1); //  updated via LotSwitcher

  return (
    <LotContext.Provider value={{ lotId, setLotId }}>
      <BrowserRouter>
        <div className={layoutStyles.layout}>
          <Banner />
          <SideImages position="left" />
          
          <div className={layoutStyles.mainArea}>
            {/* Main navigation stays above content */}
            <MainNav />

            {/* Main content */}
            <main id="main-content">
              <Routes>
                <Route path="/" element={<HomeView />} />
                <Route path="/login" element={<LoginView />} />
                <Route path="/logout" element={<LogoutView />} />
                <Route path="/register" element={<RegisterView />} />
                <Route
                  path="/userProfile"
                  element={
                    <ProtectedRoute>
                      <UserProfileView />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/valet"
                  element={
                    <ProtectedRoute roles={["ROLE_VALET", "ROLE_ADMIN"]}>
                      <ValetConsoleView />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin"
                  element={
                    <ProtectedRoute roles={["ROLE_ADMIN"]}>
                      <AdminView />
                    </ProtectedRoute>
                  }
                />
                <Route path="/unauthorized" element={<UnauthorizedView />} />
                <Route path="/pickup" element={<PatronPickupView />} />
              </Routes>
            </main>
          </div>

          <SideImages position="right" />
        </div>
      </BrowserRouter>
    </LotContext.Provider>
  );
}