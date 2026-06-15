package com.cydeo.controller;

import com.cydeo.dto.InvoiceDTO;
import com.cydeo.service.DashboardService;
import com.cydeo.service.InvoiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping()
public class DashboardController {
    private final InvoiceService invoiceService;
    private final DashboardService dashboardService;

    public DashboardController(InvoiceService invoiceService, DashboardService dashboardService) {
        this.invoiceService = invoiceService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String navigateToLanding(Model model) {
        model.addAttribute("title", "cataclysmSolutions");
        return "landing";
    }

    @GetMapping("/autoformprocessing")
    public String navigateToAutoFormProcessing(Model model) {
        model.addAttribute("title", "AutoFormProcessing | cataclysmSolutions");
        return "autoformprocessing";
    }

    @GetMapping("/customtotem")
    public String navigateToCustomTotem(Model model) {
        model.addAttribute("title", "CustomTotem | cataclysmSolutions");
        return "customtotem";
    }

    @GetMapping("/about")
    public String navigateToAbout(Model model) {
        model.addAttribute("title", "About Us | cataclysmSolutions");
        return "about";
    }

    @GetMapping("/dashboard")
    public String navigateToDashboard(Model model) {

        Map<String, BigDecimal> map = new TreeMap<>();
        map.put("totalCost", invoiceService.totalCostOfApprovedInvoices());
        map.put("totalSales", invoiceService.totalSalesOfApprovedInvoices());
        map.put("profitLoss", dashboardService.totalProfitLoss());
        List<InvoiceDTO> collect = invoiceService.listAllApprovedInvoices()
                .stream().limit(3).collect(Collectors.toList());

        model.addAttribute("summaryNumbers", map);
        model.addAttribute("invoices", collect);
        model.addAttribute("exchangeRates", dashboardService.getExchangeRates());

        return "dashboard";
    }
}
