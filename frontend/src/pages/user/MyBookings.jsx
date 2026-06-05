import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Calendar, Plus, Clock, AlignLeft, AlertCircle, Trash2, MapPin, Loader, Filter } from "lucide-react";
import bookingService from "../../services/bookingService";

const MyBookings = () => {
  const navigate = useNavigate();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [cancellingId, setCancellingId] = useState(null);
  const [filter, setFilter] = useState("ALL");

  const fetchBookings = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await bookingService.getMyBookings();
      setBookings(data);
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Failed to load bookings.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleCancel = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this booking?")) return;
    try {
      setCancellingId(id);
      await bookingService.cancelBooking(id);
      // Refresh the list
      await fetchBookings();
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Failed to cancel booking.";
      alert(msg);
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "CONFIRMED":
        return "bg-emerald-100 text-emerald-700 border border-emerald-200";
      case "PENDING":
        return "bg-amber-100 text-amber-700 border border-amber-200";
      case "CANCELLED":
        return "bg-red-100 text-red-700 border border-red-200";
      default:
        return "bg-slate-100 text-slate-700 border border-slate-200";
    }
  };

  const filteredBookings = bookings.filter((b) => filter === "ALL" || b.status === filter);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <motion.div 
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="flex flex-col items-center gap-4 bg-white p-8 rounded-3xl shadow-xl shadow-slate-200/40"
        >
          <Loader className="w-10 h-10 text-indigo-600 animate-spin" />
          <p className="text-slate-500 font-medium text-lg">Loading your bookings...</p>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="p-6 md:p-8 min-h-screen bg-slate-50/50">
      <div className="max-w-6xl mx-auto">
        <motion.div 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-8 gap-4"
        >
          <div>
            <h1 className="text-3xl font-extrabold text-slate-800 tracking-tight flex items-center gap-3">
              <Calendar className="text-indigo-600" size={32} />
              My Bookings
            </h1>
            <p className="text-slate-500 mt-2">Manage your campus resource reservations.</p>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4 w-full sm:w-auto">
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Filter size={18} className="text-slate-400" />
              </div>
              <select
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
                className="pl-10 pr-8 py-3 bg-white border-2 border-slate-200 rounded-xl text-slate-700 font-semibold focus:ring-4 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all appearance-none w-full shadow-sm cursor-pointer"
              >
                <option value="ALL">All Statuses</option>
                <option value="CONFIRMED">Confirmed</option>
                <option value="PENDING">Pending</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
              <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-slate-400">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path></svg>
              </div>
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => navigate("/dashboard/user/create-booking")}
              className="px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white font-bold rounded-xl shadow-lg shadow-indigo-200 flex items-center justify-center gap-2 transition-all whitespace-nowrap"
            >
              <Plus size={20} />
              New Booking
            </motion.button>
          </div>
        </motion.div>

        {error && (
          <motion.div 
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            className="mb-8 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-r-2xl shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4"
          >
            <div className="flex items-center gap-3">
              <AlertCircle size={24} className="text-red-500" />
              <p className="font-medium">{error}</p>
            </div>
            <button 
              onClick={fetchBookings} 
              className="px-4 py-2 bg-red-100 hover:bg-red-200 text-red-700 rounded-lg font-semibold transition-colors"
            >
              Try Again
            </button>
          </motion.div>
        )}

        {bookings.length === 0 && !error ? (
          <motion.div 
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white rounded-3xl shadow-xl shadow-slate-200/50 border border-slate-100 p-12 md:p-20 text-center"
          >
            <div className="w-24 h-24 bg-indigo-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <Calendar className="w-12 h-12 text-indigo-500" />
            </div>
            <h3 className="text-2xl font-bold text-slate-800 mb-3">No bookings yet</h3>
            <p className="text-slate-500 text-lg mb-8 max-w-md mx-auto">
              You haven't made any bookings. Create one to reserve campus resources like study rooms, labs, and equipment.
            </p>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => navigate("/dashboard/user/create-booking")}
              className="px-8 py-4 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-2xl shadow-lg shadow-indigo-200 transition-all text-lg"
            >
              Create Your First Booking
            </motion.button>
          </motion.div>
        ) : filteredBookings.length === 0 && !error ? (
           <motion.div 
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white rounded-3xl shadow-xl shadow-slate-200/50 border border-slate-100 p-12 md:p-20 text-center mt-6"
          >
            <div className="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <Filter className="w-10 h-10 text-slate-400" />
            </div>
            <h3 className="text-2xl font-bold text-slate-800 mb-3">No {filter.toLowerCase()} bookings</h3>
            <p className="text-slate-500 text-lg max-w-md mx-auto">
              You don't have any bookings matching the selected status. Try changing the filter to see more results.
            </p>
            <button
              onClick={() => setFilter("ALL")}
              className="mt-6 px-6 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-xl transition-all"
            >
              Clear Filter
            </button>
          </motion.div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
            <AnimatePresence mode="popLayout">
              {filteredBookings.map((booking, index) => (
                <motion.div 
                  layout
                  key={booking.id}
                  initial={{ opacity: 0, y: 20, scale: 0.95 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9, transition: { duration: 0.2 } }}
                  transition={{ duration: 0.3 }}
                  className="bg-white rounded-3xl shadow-lg shadow-slate-200/50 border border-slate-100 overflow-hidden flex flex-col hover:shadow-xl hover:shadow-indigo-100 transition-shadow"
                >
                  <div className="p-6 border-b border-slate-50 flex items-start justify-between">
                    <div>
                      <span className={`px-3 py-1 rounded-full text-xs font-bold tracking-wide shadow-sm ${getStatusBadge(booking.status)}`}>
                        {booking.status}
                      </span>
                      <h3 className="text-xl font-bold text-slate-800 mt-3 mb-1 line-clamp-1" title={booking.resource}>
                        {booking.resource}
                      </h3>
                      <p className="text-slate-400 text-sm font-medium">Booking #{booking.id}</p>
                    </div>
                  </div>

                  <div className="p-6 flex-grow flex flex-col gap-4 bg-slate-50/30">
                    <div className="flex items-center gap-3 text-slate-600">
                      <div className="w-10 h-10 rounded-full bg-indigo-50 flex items-center justify-center flex-shrink-0">
                        <Calendar size={18} className="text-indigo-600" />
                      </div>
                      <div>
                        <p className="text-xs text-slate-400 font-semibold uppercase">Date</p>
                        <p className="font-medium text-slate-700">{booking.date}</p>
                      </div>
                    </div>

                    <div className="flex items-center gap-3 text-slate-600">
                      <div className="w-10 h-10 rounded-full bg-purple-50 flex items-center justify-center flex-shrink-0">
                        <Clock size={18} className="text-purple-600" />
                      </div>
                      <div>
                        <p className="text-xs text-slate-400 font-semibold uppercase">Time</p>
                        <p className="font-medium text-slate-700">{booking.startTime} - {booking.endTime}</p>
                      </div>
                    </div>

                    <div className="flex items-start gap-3 text-slate-600 mt-2">
                      <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center flex-shrink-0">
                        <AlignLeft size={18} className="text-slate-500" />
                      </div>
                      <div className="flex-grow">
                        <p className="text-xs text-slate-400 font-semibold uppercase">Reason</p>
                        <p className="font-medium text-slate-700 text-sm line-clamp-2 mt-0.5" title={booking.reason}>
                          {booking.reason}
                        </p>
                      </div>
                    </div>
                  </div>

                  {booking.status !== "CANCELLED" && (
                    <div className="p-4 bg-white border-t border-slate-50">
                      <button
                        onClick={() => handleCancel(booking.id)}
                        disabled={cancellingId === booking.id}
                        className="w-full py-3 px-4 flex items-center justify-center gap-2 text-sm font-bold text-red-600 hover:text-white border-2 border-red-100 hover:bg-red-500 rounded-xl transition-all disabled:opacity-50 disabled:hover:bg-transparent disabled:hover:text-red-600"
                      >
                        {cancellingId === booking.id ? (
                          <>
                            <Loader size={18} className="animate-spin" />
                            Cancelling...
                          </>
                        ) : (
                          <>
                            <Trash2 size={18} />
                            Cancel Booking
                          </>
                        )}
                      </button>
                    </div>
                  )}
                </motion.div>
              ))}
            </AnimatePresence>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyBookings;
