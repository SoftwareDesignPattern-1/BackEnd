package com.bulgumtong;

import com.bulgumtong.init.DataInitializer;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.pattern.command.CommandHistory;
import com.bulgumtong.pattern.proxy.AuthProxy;
import com.bulgumtong.pattern.proxy.StudyService;
import com.bulgumtong.pattern.proxy.StudyServiceImpl;
import com.bulgumtong.pattern.proxy.UnauthorizedException;
import com.bulgumtong.report.ReportPrinter;
import com.bulgumtong.repository.AttendanceRepository;
import com.bulgumtong.repository.MemberRepository;
import com.bulgumtong.service.AttendanceService;
import com.bulgumtong.service.FineCalculatorService;
import com.sun.net.httpserver.HttpExchange;
import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.Member;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static StudyService studyService;
    private static ReportPrinter reportPrinter;

    public static void main(String[] args) throws IOException {
        MemberRepository     memberRepo    = new MemberRepository();
        AttendanceRepository attendanceRepo = new AttendanceRepository();
        CommandHistory       cmdHistory    = new CommandHistory();

        AttendanceService   attendanceSvc = new AttendanceService(attendanceRepo, memberRepo, cmdHistory);
        FineCalculatorService fineSvc     = new FineCalculatorService(attendanceRepo);
        StudyServiceImpl    realService   = new StudyServiceImpl(memberRepo, attendanceSvc);

        studyService  = new AuthProxy(realService, memberRepo);
        reportPrinter = new ReportPrinter(memberRepo, fineSvc);

        new DataInitializer(memberRepo, attendanceRepo).load();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/members",    Main::handleMembers);
        server.createContext("/api/attendance", Main::handleAttendance);
        server.createContext("/api/history",    Main::handleHistory);
        server.createContext("/api/report",     Main::handleReport);
        server.setExecutor(null);
        server.start();

        System.out.println("bulgumtong 서버 시작 → http://localhost:8080");
        System.out.println("  GET  /api/members");
        System.out.println("  POST /api/members?requesterId=&name=&role=LEADER|MEMBER");
        System.out.println("  GET  /api/attendance?requesterId=&memberId=");
        System.out.println("  POST /api/attendance?requesterId=&memberId=&date=YYYY-MM-DD&status=PRESENT|ABSENT|LATE");
        System.out.println("  GET  /api/history?requesterId=");
        System.out.println("  GET  /api/report?requesterId=");
    }

    private static void handleMembers(HttpExchange ex) throws IOException {
        try {
            if ("GET".equals(ex.getRequestMethod())) {
                List<Member> members = studyService.getAllMembers();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < members.size(); i++) {
                    var m = members.get(i);
                    if (i > 0) sb.append(",");
                    sb.append(String.format(
                        "{\"id\":\"%s\",\"name\":\"%s\",\"role\":\"%s\"}",
                        m.getId(), m.getName(), m.getRole().getLabel()));
                }
                respond(ex, 200, sb.append("]").toString());

            } else if ("POST".equals(ex.getRequestMethod())) {
                Map<String, String> p = parseQuery(ex.getRequestURI());
                var member = studyService.addMember(
                    require(p, "requesterId"),
                    require(p, "name"),
                    p.getOrDefault("role", "MEMBER"));
                respond(ex, 201, String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"role\":\"%s\"}",
                    member.getId(), member.getName(), member.getRole().getLabel()));
            } else {
                respond(ex, 405, err("Method Not Allowed"));
            }
        } catch (UnauthorizedException e) {
            respond(ex, 403, err(e.getMessage()));
        } catch (Exception e) {
            respond(ex, 400, err(e.getMessage()));
        }
    }

    private static void handleAttendance(HttpExchange ex) throws IOException {
        try {
            Map<String, String> p = parseQuery(ex.getRequestURI());
            String requesterId = require(p, "requesterId");

            if ("GET".equals(ex.getRequestMethod())) {
                var records = studyService.getRecordsByMember(requesterId, require(p, "memberId"));
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < records.size(); i++) {
                    var r = records.get(i);
                    if (i > 0) sb.append(",");
                    sb.append(String.format(
                        "{\"memberId\":\"%s\",\"date\":\"%s\",\"status\":\"%s\"}",
                        r.getMemberId(), r.getSessionDate(), r.getStatus().getLabel()));
                }
                respond(ex, 200, sb.append("]").toString());

            } else if ("POST".equals(ex.getRequestMethod())) {
                LocalDate date     = LocalDate.parse(require(p, "date"));
                AttendanceStatus s = AttendanceStatus.valueOf(require(p, "status").toUpperCase());
                var record = studyService.checkAttendance(requesterId, require(p, "memberId"), date, s);
                respond(ex, 200, String.format(
                    "{\"memberId\":\"%s\",\"date\":\"%s\",\"status\":\"%s\"}",
                    record.getMemberId(), record.getSessionDate(), record.getStatus().getLabel()));
            } else {
                respond(ex, 405, err("Method Not Allowed"));
            }
        } catch (UnauthorizedException e) {
            respond(ex, 403, err(e.getMessage()));
        } catch (Exception e) {
            respond(ex, 400, err(e.getMessage()));
        }
    }

    private static void handleHistory(HttpExchange ex) throws IOException {
        try {
            Map<String, String> p = parseQuery(ex.getRequestURI());
            var history = studyService.getCommandHistory(require(p, "requesterId"));
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < history.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escape(history.get(i))).append("\"");
            }
            respond(ex, 200, sb.append("]").toString());
        } catch (UnauthorizedException e) {
            respond(ex, 403, err(e.getMessage()));
        } catch (Exception e) {
            respond(ex, 400, err(e.getMessage()));
        }
    }

    private static void handleReport(HttpExchange ex) throws IOException {
        try {
            Map<String, String> p = parseQuery(ex.getRequestURI());
            studyService.getAllRecords(require(p, "requesterId")); // 권한 확인
            respond(ex, 200, String.format("{\"report\":\"%s\"}", escape(reportPrinter.generateFineReport())));
        } catch (UnauthorizedException e) {
            respond(ex, 403, err(e.getMessage()));
        } catch (Exception e) {
            respond(ex, 400, err(e.getMessage()));
        }
    }

    private static void respond(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String query = uri.getQuery();
        if (query == null) return result;
        Arrays.stream(query.split("&")).forEach(pair -> {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) result.put(kv[0], kv[1]);
        });
        return result;
    }

    private static String require(Map<String, String> p, String key) {
        String v = p.get(key);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("필수 파라미터 누락: " + key);
        return v;
    }

    private static String err(String msg) {
        return String.format("{\"error\":\"%s\"}", escape(msg));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
