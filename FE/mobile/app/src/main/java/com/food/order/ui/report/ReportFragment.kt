package com.food.order.ui.report

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import android.webkit.WebView
import androidx.navigation.fragment.findNavController
import com.food.order.data.response.RevenueByWeek
import com.food.order.databinding.FragmentReportBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by viewModels()

    // giữ 1 chart view & 1 chart duy nhất
    private var chartView: AnyChartView? = null
    private var pie: Pie? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            launch { viewModel.countEmployeeFlow.collectLatest { binding.tvUserNumber.text = it.toString() } }
            launch { viewModel.timeFlow.collectLatest { binding.tvMonthYear.text = it } }
            launch { viewModel.mostFavoriteFoodFlow.collectLatest { binding.tvMostFood.text = it?.foodName ?: "No data" } }
            launch {
                viewModel.listOrderInTimeFlow.collectLatest { list ->
                    if (list != null) {
                        binding.tvPaymentInvoiceNumber.text = list.size.toString()
                        var ordering = 0; var completed = 0; var cancelled = 0
                        for (o in list) when (o.status) {
                            "ORDERING" -> ordering++
                            "COMPLETED" -> completed++
                            "CANCELLED" -> cancelled++
                        }
                        binding.tvCountOrdering.text = ordering.toString()
                        binding.tvCountCompleted.text = completed.toString()
                        binding.tvCountCancelled.text = cancelled.toString()
                    } else {
                        binding.tvPaymentInvoiceNumber.text = "0"
                        binding.tvCountOrdering.text = "0"
                        binding.tvCountCompleted.text = "0"
                        binding.tvCountCancelled.text = "0"
                    }
                }
            }
            launch {
                viewModel.revenueByWeekFlow.collectLatest { weeks ->
                    if (!isAdded || _binding == null) return@collectLatest
                    if (weeks.isNullOrEmpty()) {
                        destroyChartView()
                        binding.layoutAnyChartView.removeAllViews()
                        binding.layoutAnyChartView.visibility = View.INVISIBLE
                    } else {
                        binding.layoutAnyChartView.visibility = View.VISIBLE
                        setupPieChart(weeks)
                    }
                }
            }
            launch {
                viewModel.errorFlow.collectLatest {
                    if (isAdded) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        viewModel.server = sharedPref.getString("server_address", "") ?: ""
        viewModel.authToken = "Bearer " + (sharedPref.getString("token", "") ?: "")

        viewModel.fetchDataInTime()
        binding.ivPrev.setOnClickListener { viewModel.prevTime() }
        binding.ivNext.setOnClickListener { viewModel.nextTime() }
        binding.cardViewBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupPieChart(result: List<RevenueByWeek>) {
        if (!isAdded || _binding == null) return

        destroyChartView()

        val data: MutableList<DataEntry> = ArrayList()
        for (r in result) data.add(ValueDataEntry("Week ${r.week}", r.total))

        val pieChart = pie ?: AnyChart.pie().apply {
            title("")
            labels().position("outside")
            legend().title().enabled(true)
            legend().title().text("Week").padding(0.0, 0.0, 10.0, 0.0)
            legend().position("center-bottom").itemsLayout(LegendLayout.HORIZONTAL).align(Align.CENTER)
        }
        pie = pieChart
        pieChart.data(data)

        // Tạo AnyChartView mới và gán chart ngay lập tức trước khi thêm vào layout
        // để tránh crash NullPointerException trong onPageFinished của WebView
        val cv = AnyChartView(requireContext()).apply {
            setProgressBar(binding.progressBar)
            setChart(pieChart)
        }

        binding.layoutAnyChartView.removeAllViews()
        binding.layoutAnyChartView.addView(cv)
        chartView = cv
    }

    private fun destroyChartView() {
        val cv = chartView ?: return
        chartView = null
        try {
            cv.clear()
            val webViewId = resources.getIdentifier("web_view", "id", requireContext().packageName)
            if (webViewId != 0) {
                val webView = cv.findViewById<WebView>(webViewId)
                webView?.apply {
                    stopLoading()
                    webViewClient = android.webkit.WebViewClient()
                    webChromeClient = android.webkit.WebChromeClient()
                    destroy()
                }
            }
        } catch (_: Throwable) {}
    }

    override fun onDestroyView() {
        destroyChartView()
        pie = null
        _binding = null
        super.onDestroyView()
    }
}
