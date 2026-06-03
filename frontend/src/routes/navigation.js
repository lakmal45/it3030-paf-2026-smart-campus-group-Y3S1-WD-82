import {
  LayoutDashboard,
  Calendar,
  PlusCircle,
  Ticket,
  User,
  Users,
  Shield,
  Settings,
  FileText,
  BarChart,
  Wrench,
  CheckSquare,
  History,
  Box,
  ClipboardList,
} from "lucide-react";

export const roleNavigation = {
  USER: [
    { name: "Overview", path: "/dashboard/user", icon: LayoutDashboard },
    { name: "My Bookings", path: "/dashboard/user/bookings", icon: Calendar },
    {
      name: "Create Booking",
      path: "/dashboard/user/create-booking",
      icon: PlusCircle,
    },
    { name: "My Tickets", path: "/dashboard/user/tickets", icon: Ticket },
    {
      name: "Report Incident",
      path: "/dashboard/user/tickets/new",
      icon: Wrench,
    },
    { name: "Resources", path: "/dashboard/user/resources", icon: Box },
  ],
  ADMIN: [
    { name: "Dashboard", path: "/dashboard/admin", icon: LayoutDashboard },
    { name: "All Bookings", path: "/dashboard/admin/bookings", icon: Calendar },
    { name: "User Management", path: "/dashboard/admin/users", icon: Users },
    { name: "All Bookings", path: "/dashboard/admin/bookings", icon: Calendar },
    { name: "All Tickets", path: "/dashboard/admin/tickets", icon: Ticket },
    { name: "Resources", path: "/dashboard/admin/resources", icon: Box },
    {
      name: "Activity Log",
      path: "/dashboard/admin/activity-log",
      icon: ClipboardList,
    },
  ],
  MANAGER: [
    { name: "Dashboard", path: "/dashboard/manager", icon: LayoutDashboard },
    { name: "All Tickets", path: "/dashboard/manager/tickets", icon: Ticket },
    { name: "Reports", path: "/dashboard/manager/reports", icon: FileText },
    {
      name: "Booking Analytics",
      path: "/dashboard/manager/analytics",
      icon: BarChart,
    },
    {
      name: "Maintenance",
      path: "/dashboard/manager/maintenance",
      icon: Wrench,
    },
    { name: "Resources", path: "/dashboard/manager/resources", icon: Box },
  ],
  TECHNICIAN: [
    { name: "Dashboard", path: "/dashboard/technician", icon: LayoutDashboard },
    {
      name: "Assigned Tickets",
      path: "/dashboard/technician/tickets",
      icon: Ticket,
    },
    {
      name: "Update Status",
      path: "/dashboard/technician/update-status",
      icon: CheckSquare,
    },
    { name: "History", path: "/dashboard/technician/history", icon: History },
  ],
};
