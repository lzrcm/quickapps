package com.yoavst.quickapps.notifications

import android.annotation.SuppressLint
import android.app.Fragment
import android.app.Notification
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.yoavst.kotlin.hide
import com.yoavst.quickapps.R
import com.yoavst.quickapps.calendar.isToday
import com.yoavst.quickapps.calendar.isYesterday
import com.yoavst.quickapps.tools.QCircleActivity
import com.yoavst.quickapps.tools.amPmInNotifications
import com.yoavst.quickapps.tools.notificationShowContent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Created by Yoav.
 */
public class NotificationsFragment : Fragment() {
    public var notification: StatusBarNotification? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.notification_fragment, container, false)
        val notificationIcon = view.findViewById(R.id.notification_icon) as ImageView
        val notificationTime = view.findViewById(R.id.notification_time) as TextView
        val notificationTitle = view.findViewById(R.id.notification_title) as TextView
        val notificationText = view.findViewById(R.id.notification_text) as TextView
        val delete = view.findViewById(R.id.delete) as TextView
        notification = getArguments().getParcelable(NOTIFICATION)
        delete.setOnClickListener { (getActivity() as CNotificationActivity).cancelNotification(notification) }
        view.setOnTouchListener { view, motionEvent -> (getActivity() as QCircleActivity).gestureDetector.onTouchEvent(motionEvent) }
        if (today == null || yesterday == null) {
            today = getString(R.string.today)
            yesterday = getString(R.string.yesterday)
        }
        if (notification != null) {
            delete.setVisibility(if (notification!!.isClearable()) View.VISIBLE else View.GONE)
            val extras = notification!!.getNotification().extras
            notificationTitle.setText(extras.getCharSequence(Notification.EXTRA_TITLE))
            try {
                notificationIcon.setImageDrawable(getActivity().createPackageContext(notification!!.getPackageName(), 0).getResources().getDrawable(notification!!.getNotification().icon))
            } catch (ignored: PackageManager.NameNotFoundException) {
            } catch (ignored: Resources.NotFoundException) {
            }

            if (getActivity().notificationShowContent) {
                val preText = extras.getCharSequence(Notification.EXTRA_TEXT)
                var text: String? = preText?.toString()
                if (text == null || text.length() == 0) {
                    val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                    if (lines != null) {
                        text = ""
                        val newline = System.getProperty("line.separator").toString()
                        for (line in lines) {
                            text += (line.toString() + newline)
                        }
                    }
                }
                notificationText.setText(text)
            }
            val time = notification!!.getPostTime()
            val date = Date(time)
            val calendar = Calendar.getInstance()
            calendar.setTimeInMillis(time)
            if (date.isToday()) {
                notificationTime.setText(today + " " + parseHour(date, getActivity().amPmInNotifications))
            } else if (calendar.isYesterday()) {
                notificationTime.setText(yesterday + " " + parseHour(date, getActivity().amPmInNotifications))
            } else {
                notificationTime.setText(dayFormatter.format(date) + ", " + parseHour(date, getActivity().amPmInNotifications))
            }
        } else
            delete.hide()
        return view
    }

    fun parseHour(date: Date, isAmPm: Boolean): String {
        return if (isAmPm) hourFormatterAmPm.format(date) else hourFormatter.format(date)
    }

    companion object {
        var today: String? = null
        var yesterday: String? = null
        SuppressLint("SimpleDateFormat")
        private val dayFormatter = SimpleDateFormat("MMM d")
        SuppressLint("SimpleDateFormat")
        private val hourFormatter = SimpleDateFormat("HH:mm")
        SuppressLint("SimpleDateFormat")
        private val hourFormatterAmPm = SimpleDateFormat("hh:mm a")

        var NOTIFICATION: String = "notification"

        public fun newInstance(notification: StatusBarNotification): NotificationsFragment {
            var fragment = NotificationsFragment()
            var args = Bundle()
            args.putParcelable(NOTIFICATION, notification)
            fragment.setArguments(args)
            return fragment;
        }
    }
}
