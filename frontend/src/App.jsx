import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import "./index.css";

// Context
import { AuthProvider, useAuth } from "./context/AuthContext";

// Pages
import Home from "./pages/Home";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import OAuthSuccess from "./components/OAuthSuccess";
import GeneralProfile from "./pages/Profile";
import Settings from "./pages/Settings";

// Layout & Protection
import DashboardLayout from "./layout/DashboardLayout";
import ProtectedRoute from "./components/RoleProtectedRoute";
import { NotificationProvider } from "./context/NotificationContext";

// Dashboards
import UserDashboard from "./pages/user/UserDashboard";
import AdminDashboard from "./pages/admin/AdminDashboard";
import ManagerDashboard from "./pages/manager/ManagerDashboard";
import TechnicianDashboard from "./pages/technician/TechnicianDashboard";
import ResourceUserDashboard from "./pages/user/ResourceUserDashboard";
import ResourceAdminDashboard from "./pages/admin/ResourceAdminDashboard";

// Notifications
import NotificationsPage from "./pages/NotificationsPage";

// User Pages
import MyBookings from "./pages/user/MyBookings";
import CreateBooking from "./pages/user/CreateBooking";
import MyTickets from "./pages/user/MyTickets";
import CreateTicketForm from "./components/tickets/CreateTicketForm";
import EditTicketForm from "./components/tickets/EditTicketForm";
import TicketDetail from "./components/tickets/TicketDetail";

// Admin Pages
import UserManagement from "./pages/admin/UserManagement";
import AllTickets from "./pages/admin/AllTickets";
import AllBookings from "./pages/admin/AllBookings";
 origin/member_2_bookingManagement

import ActivityLog from "./pages/admin/ActivityLog";
main

// Manager Pages
import Reports from "./pages/manager/Reports";
import BookingAnalytics from "./pages/manager/BookingAnalytics";
import Maintenance from "./pages/manager/Maintenance";
import ManagerTickets from "./pages/manager/ManagerTickets";
import ResourceListPage from "./pages/manager/ResourceListPage";
import ResourceDetailPage from "./pages/manager/ResourceDetailPage";
import ResourceFormPage from "./pages/manager/ResourceFormPage";

// Technician Pages
import AssignedTickets from "./pages/technician/AssignedTickets";
import UpdateStatus from "./pages/technician/UpdateStatus";
import History from "./pages/technician/History";

// A smart component to redirect to the correct dashboard based on role
const RoleBasedRedirect = () => {
  const { user, role } = useAuth();

  if (!user) return <Navigate to="/login" replace />;

  switch (role) {
    case "ADMIN":
    case "ROLE_ADMIN":
      return <Navigate to="/dashboard/admin" replace />;
    case "MANAGER":
    case "ROLE_MANAGER":
      return <Navigate to="/dashboard/manager" replace />;
    case "TECHNICIAN":
    case "ROLE_TECHNICIAN":
      return <Navigate to="/dashboard/technician" replace />;
    default:
      return <Navigate to="/dashboard/user" replace />;
  }
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NotificationProvider>
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/oauth-success" element={<OAuthSuccess />} />

            {/* Root */}
            <Route path="/" element={<Home />} />

            {/* Protected Routes wrapped in Dashboard Layout */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DashboardLayout />
                </ProtectedRoute>
              }
            >
              {/* The index route does the smart role redirection */}
              <Route index element={<RoleBasedRedirect />} />

              {/* User Routes */}
              <Route path="user">
                <Route
                  index
                  element={
                    <ProtectedRoute allowedRoles={["USER", "ROLE_USER"]}>
                      <UserDashboard />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="bookings"
                  element={
                    <ProtectedRoute allowedRoles={["USER", "ROLE_USER"]}>
                      <MyBookings />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="create-booking"
                  element={
                    <ProtectedRoute allowedRoles={["USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN", "MANAGER", "ROLE_MANAGER"]}>
                      <CreateBooking />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets"
                  element={
                    <ProtectedRoute allowedRoles={["USER", "ROLE_USER"]}>
                      <MyTickets />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets/new"
                  element={
                    <ProtectedRoute allowedRoles={["USER", "ROLE_USER"]}>
                      <CreateTicketForm />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets/:id/edit"
                  element={
                    <ProtectedRoute allowedRoles={["USER", "ROLE_USER"]}>
                      <EditTicketForm />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets/:id"
                  element={
                    <ProtectedRoute allowedRoles={["USER", "ROLE_USER"]}>
                      <TicketDetail />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources"
                  element={
                    <ProtectedRoute
                      allowedRoles={[
                        "USER",
                        "ROLE_USER",
                        "ADMIN",
                        "ROLE_ADMIN",
                        "MANAGER",
                        "ROLE_MANAGER",
                      ]}
                    >
                      <ResourceListPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/new"
                  element={
                    <ProtectedRoute
                      allowedRoles={[
                        "USER",
                        "ROLE_USER",
                        "ADMIN",
                        "ROLE_ADMIN",
                        "MANAGER",
                        "ROLE_MANAGER",
                      ]}
                    >
                      <ResourceFormPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/:id"
                  element={
                    <ProtectedRoute
                      allowedRoles={[
                        "USER",
                        "ROLE_USER",
                        "ADMIN",
                        "ROLE_ADMIN",
                        "MANAGER",
                        "ROLE_MANAGER",
                      ]}
                    >
                      <ResourceDetailPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/:id/edit"
                  element={
                    <ProtectedRoute
                      allowedRoles={[
                        "USER",
                        "ROLE_USER",
                        "ADMIN",
                        "ROLE_ADMIN",
                        "MANAGER",
                        "ROLE_MANAGER",
                      ]}
                    >
                      <ResourceFormPage />
                    </ProtectedRoute>
                  }
                />
              </Route>

 origin/member_2_bookingManagement
            {/* Admin Routes */}
            <Route path="admin">
              <Route
                index
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <AdminDashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="bookings"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <AllBookings />
                  </ProtectedRoute>
                }
              />
              <Route
                path="users"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <UserManagement />
                  </ProtectedRoute>
                }
              />
              <Route
                path="roles"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <RoleManagement />
                  </ProtectedRoute>
                }
              />
              <Route
                path="settings"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <SystemSettings />
                  </ProtectedRoute>
                }
              />
              <Route
                path="tickets"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <AllTickets />
                  </ProtectedRoute>
                }
              />
              <Route
                path="tickets/:id"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <TicketDetail />
                  </ProtectedRoute>
                }
              />
              <Route
                path="resources"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <ResourceListPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="resources/new"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <ResourceFormPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="resources/:id"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <ResourceDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="resources/:id/edit"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <ResourceFormPage />
                  </ProtectedRoute>
                }
              />
            </Route>

              {/* Admin Routes */}
              <Route path="admin">
                <Route
                  index
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <AdminDashboard />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="users"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <UserManagement />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <AllTickets />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets/:id"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <TicketDetail />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets/:id/edit"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <EditTicketForm />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <ResourceListPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/new"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <ResourceFormPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/:id"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <ResourceDetailPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/:id/edit"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <ResourceFormPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="bookings"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <AllBookings />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="activity-log"
                  element={
                    <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                      <ActivityLog />
                    </ProtectedRoute>
                  }
                />
              </Route>
 main

              {/* Manager Routes */}
              <Route path="manager">
                <Route
                  index
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <ManagerDashboard />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="reports"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <Reports />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="analytics"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <BookingAnalytics />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="maintenance"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <Maintenance />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <ManagerTickets />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets/:id"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <TicketDetail />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <ResourceListPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/new"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <ResourceFormPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/:id"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <ResourceDetailPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="resources/:id/edit"
                  element={
                    <ProtectedRoute allowedRoles={["MANAGER", "ROLE_MANAGER"]}>
                      <ResourceFormPage />
                    </ProtectedRoute>
                  }
                />
              </Route>

              {/* Technician Routes */}
              <Route path="technician">
                <Route
                  index
                  element={
                    <ProtectedRoute
                      allowedRoles={["TECHNICIAN", "ROLE_TECHNICIAN"]}
                    >
                      <TechnicianDashboard />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets"
                  element={
                    <ProtectedRoute
                      allowedRoles={["TECHNICIAN", "ROLE_TECHNICIAN"]}
                    >
                      <AssignedTickets />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="tickets/:id"
                  element={
                    <ProtectedRoute
                      allowedRoles={["TECHNICIAN", "ROLE_TECHNICIAN"]}
                    >
                      <TicketDetail />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="update-status"
                  element={
                    <ProtectedRoute
                      allowedRoles={["TECHNICIAN", "ROLE_TECHNICIAN"]}
                    >
                      <UpdateStatus />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="history"
                  element={
                    <ProtectedRoute
                      allowedRoles={["TECHNICIAN", "ROLE_TECHNICIAN"]}
                    >
                      <History />
                    </ProtectedRoute>
                  }
                />
              </Route>

              {/* Shared Dashboard Routes */}
              <Route path="profile" element={<GeneralProfile />} />
              <Route path="settings" element={<Settings />} />
              <Route path="notifications" element={<NotificationsPage />} />
              {/* Resource-Specific Dashboard Routes */}
              <Route
                path="user/resource-dashboard"
                element={
                  <ProtectedRoute allowedRoles={["USER", "ROLE_USER"]}>
                    <ResourceUserDashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/resource-dashboard"
                element={
                  <ProtectedRoute allowedRoles={["ADMIN", "ROLE_ADMIN"]}>
                    <ResourceAdminDashboard />
                  </ProtectedRoute>
                }
              />
            </Route>

            {/* Fallback Unauthorized/Not Found */}
            <Route
              path="/unauthorized"
              element={
                <div className="p-10 text-center text-red-600 font-bold">
                  Unauthorized Access
                </div>
              }
            />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
