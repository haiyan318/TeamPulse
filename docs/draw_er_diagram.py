# -*- coding: utf-8 -*-
"""
TeamPulse ER Diagram - Visio COM automation via Python win32com
Draws 11 tables + 1 view with relationship lines, exports as PNG.
"""

import win32com.client
import pythoncom
import os

OUTPUT_DIR = r"D:\code_local\IdeaProjects\SoftwareEngineering\TeamPulse\docs"
PNG_PATH = os.path.join(OUTPUT_DIR, "er_diagram.png")
VSDX_PATH = os.path.join(OUTPUT_DIR, "er_diagram.vsdx")

# Entity definitions: (name, x, y, width, fields[], header_color, body_color)
# Colors as RGB tuples
C_A_HDR = (31, 78, 121)    # dark blue
C_A_BODY = (221, 235, 247)  # light blue
C_B_HDR = (0, 109, 86)      # dark teal
C_B_BODY = (209, 242, 235)  # light teal
C_C_HDR = (112, 48, 160)    # dark purple
C_C_BODY = (242, 228, 246)  # light purple
C_V_HDR = (120, 120, 120)   # gray
C_V_BODY = (238, 238, 238)   # light gray
C_LINE = (130, 130, 130)
C_WHITE = (255, 255, 255)

def rgb_str(rgb):
    return f"RGB({rgb[0]},{rgb[1]},{rgb[2]})"

def field_height(n_fields):
    """Calculate body height based on number of fields"""
    return 0.2 * n_fields + 0.15

def draw_entity(page, name, x, y, w, fields, hdr_color, body_color):
    """Draw an entity box with header + body.
    (x, y) = bottom-left corner, y grows upward.
    Returns (header_shape, body_shape) for connection purposes."""
    hdr_h = 0.38
    body_h = field_height(len(fields))
    total_h = hdr_h + body_h

    # Header rectangle (top portion)
    hdr = page.DrawRectangle(x, y + body_h, x + w, y + total_h)
    hdr.Cells("FillForegnd").FormulaU = rgb_str(hdr_color)
    hdr.Cells("LineColor").FormulaU = rgb_str(hdr_color)
    hdr.Text = name
    hdr.Cells("Char.Font").FormulaU = '"Microsoft YaHei"'
    hdr.Cells("Char.Size").FormulaU = "10 pt"
    hdr.Cells("Char.Style").FormulaU = "1"
    hdr.Cells("Char.Color").FormulaU = rgb_str(C_WHITE)
    hdr.Cells("Para.HorzAlign").FormulaU = "1"
    hdr.Cells("VerticalAlign").FormulaU = "1"

    # Body rectangle (bottom portion)
    body = page.DrawRectangle(x, y, x + w, y + body_h)
    body.Cells("FillForegnd").FormulaU = rgb_str(body_color)
    body.Cells("LineColor").FormulaU = rgb_str(hdr_color)
    field_text = "\n".join(fields)
    body.Text = field_text
    body.Cells("Char.Font").FormulaU = '"Consolas"'
    body.Cells("Char.Size").FormulaU = "8 pt"
    body.Cells("Char.Color").FormulaU = "RGB(50,50,50)"
    body.Cells("VerticalAlign").FormulaU = "0"
    body.Cells("Para.HorzAlign").FormulaU = "0"

    return (hdr, body, total_h)

def connect_shapes(page, from_shape, to_shape, label=""):
    """Draw a connection line between two shapes with optional label."""
    from_x = float(from_shape.Cells("PinX").Result(""))
    from_y = float(from_shape.Cells("PinY").Result(""))
    to_x = float(to_shape.Cells("PinX").Result(""))
    to_y = float(to_shape.Cells("PinY").Result(""))

    connector = page.DrawLine(from_x, from_y, to_x, to_y)
    connector.Cells("LineColor").FormulaU = rgb_str(C_LINE)
    connector.Cells("LineWeight").FormulaU = "0.75 pt"
    if label:
        connector.Text = label
        connector.Cells("Char.Size").FormulaU = "7 pt"
        connector.Cells("Char.Color").FormulaU = rgb_str(C_LINE)
        connector.Cells("Para.HorzAlign").FormulaU = "1"
    return connector


def main():
    print("Starting Visio COM automation...")

    pythoncom.CoInitialize()
    visio = win32com.client.Dispatch("Visio.Application")
    visio.Visible = False

    try:
        doc = visio.Documents.Add("")
        page = doc.Pages(1)

        # Page setup: large landscape
        page.PageSheet.Cells("PageWidth").FormulaU = "22 in"
        page.PageSheet.Cells("PageHeight").FormulaU = "14 in"
        page.PageSheet.Cells("DrawingScale").FormulaU = "1 in"
        page.PageSheet.Cells("PageScale").FormulaU = "1 in"
        print("Page setup complete.")

        ew = 3.3  # entity width

        # === Module A (left column, x=0.5) ===
        xa = 0.5

        r_sysRole = draw_entity(page, "sys_role", xa, 9.5, ew,
            ["PK  id", "UQ  role_code", "     role_name", "     description"],
            C_A_HDR, C_A_BODY)

        r_sysUser = draw_entity(page, "sys_user", xa, 5.0, ew,
            ["PK  id", "UQ  username", "     password (BCrypt)", "     email",
             "     phone", "     real_name", "     student_id", "     status"],
            C_A_HDR, C_A_BODY)

        r_sysUserRole = draw_entity(page, "sys_user_role", xa, 2.0, ew,
            ["PK  id", "FK  user_id -> sys_user", "FK  role_id -> sys_role"],
            C_A_HDR, C_A_BODY)

        # === Module B (center column, x=5.5) ===
        xb = 5.5

        r_course = draw_entity(page, "course", xb, 9.5, ew,
            ["PK  id", "     course_name", "     course_code",
             "FK  teacher_id -> sys_user", "     status"],
            C_B_HDR, C_B_BODY)

        r_project = draw_entity(page, "project", xb, 5.0, ew,
            ["PK  id", "     project_name", "FK  course_id -> course",
             "     start_date", "     end_date", "     status",
             "FK  creator_id -> sys_user"],
            C_B_HDR, C_B_BODY)

        r_projectGroup = draw_entity(page, "project_group", xb, 3.0, ew,
            ["PK  id", "     group_name", "FK  project_id -> project",
             "FK  leader_id -> sys_user", "     status"],
            C_B_HDR, C_B_BODY)

        r_groupMember = draw_entity(page, "group_member", xb, 0.5, ew,
            ["PK  id", "FK  group_id -> project_group",
             "FK  user_id -> sys_user", "     role_in_group"],
            C_B_HDR, C_B_BODY)

        # === Module C (right column, x=10.5) ===
        xc = 10.5

        r_taskAssignment = draw_entity(page, "task_assignment", xc, 10.0, ew,
            ["PK  id", "FK  task_id -> task", "FK  user_id -> sys_user"],
            C_C_HDR, C_C_BODY)

        r_task = draw_entity(page, "task", xc, 5.0, ew,
            ["PK  id", "     task_name", "     task_code",
             "FK  project_id -> project", "FK  group_id -> project_group",
             "     status", "FK  assignee_id -> sys_user",
             "     plan_end_time", "     priority"],
            C_C_HDR, C_C_BODY)

        r_taskDependency = draw_entity(page, "task_dependency", xc, 3.0, ew,
            ["PK  id", "FK  task_id -> task", "FK  prerequisite_task_id -> task"],
            C_C_HDR, C_C_BODY)

        r_taskStatusLog = draw_entity(page, "task_status_log", xc, 0.5, ew,
            ["PK  id", "FK  task_id -> task", "     from_status",
             "     to_status", "FK  operator_id -> sys_user"],
            C_C_HDR, C_C_BODY)

        # === Module D / View (far right, x=15.5) ===
        xd = 15.5

        r_view = draw_entity(page, "v_project_progress (VIEW)", xd, 5.0, ew,
            ["     project_id", "     project_name", "     total_tasks",
             "     not_started_count", "     in_progress_count",
             "     reviewing_count", "     done_count",
             "     overdue_count", "     due_this_week_count"],
            C_V_HDR, C_V_BODY)

        print("All entity boxes drawn. Drawing connections...")

        # === Connections ===
        connect_shapes(page, r_sysUser[1], r_sysUserRole[1], "1 : N")
        connect_shapes(page, r_sysRole[1], r_sysUserRole[1], "1 : N")
        connect_shapes(page, r_sysUser[1], r_course[1], "1 : N")
        connect_shapes(page, r_course[1], r_project[1], "1 : N")
        connect_shapes(page, r_project[1], r_projectGroup[1], "1 : N")
        connect_shapes(page, r_projectGroup[1], r_groupMember[1], "1 : N")
        connect_shapes(page, r_sysUser[1], r_groupMember[1], "1 : N")
        connect_shapes(page, r_project[1], r_task[1], "1 : N")
        connect_shapes(page, r_projectGroup[1], r_task[1], "1 : N")
        connect_shapes(page, r_sysUser[1], r_task[1], "1 : N")
        connect_shapes(page, r_task[1], r_taskAssignment[1], "1 : N")
        connect_shapes(page, r_sysUser[1], r_taskAssignment[1], "1 : N")
        connect_shapes(page, r_task[1], r_taskDependency[1], "1 : N")
        connect_shapes(page, r_task[1], r_taskStatusLog[1], "1 : N")
        connect_shapes(page, r_task[1], r_view[1], "")

        # === Title ===
        title = page.DrawRectangle(5.5, 12.3, 16.5, 13.2)
        title.Cells("FillForegnd").FormulaU = rgb_str(C_WHITE)
        title.Cells("LineColor").FormulaU = rgb_str(C_WHITE)
        title.Text = "TeamPulse 数据库 ER 关系图（第一阶段 v0.1）"
        title.Cells("Char.Font").FormulaU = '"Microsoft YaHei"'
        title.Cells("Char.Size").FormulaU = "16 pt"
        title.Cells("Char.Style").FormulaU = "1"
        title.Cells("Char.Color").FormulaU = "RGB(31,54,100)"
        title.Cells("Para.HorzAlign").FormulaU = "1"

        # === Module legends ===
        leg_w = 3.0
        legends = [
            (0.5, 12.3, "模块 A 用户与权限", C_A_HDR),
            (4.5, 12.3, "模块 B 课程/项目/小组", C_B_HDR),
            (16.5, 12.3, "模块 C 任务管理", C_C_HDR),
        ]
        for lx, ly, ltext, lcolor in legends:
            leg = page.DrawRectangle(lx, ly, lx + leg_w, ly + 0.45)
            leg.Cells("FillForegnd").FormulaU = rgb_str(lcolor)
            leg.Cells("LineColor").FormulaU = rgb_str(lcolor)
            leg.Text = ltext
            leg.Cells("Char.Font").FormulaU = '"Microsoft YaHei"'
            leg.Cells("Char.Size").FormulaU = "8 pt"
            leg.Cells("Char.Style").FormulaU = "1"
            leg.Cells("Char.Color").FormulaU = rgb_str(C_WHITE)
            leg.Cells("Para.HorzAlign").FormulaU = "1"

        print("Title and legends added. Exporting...")

        # Make view shape dashed border
        r_view[1].Cells("LinePattern").FormulaU = "2"
        r_view[0].Cells("LinePattern").FormulaU = "2"

        # === Export PNG (high resolution) ===
        try:
            visio.Settings.RasterExportResolution = 2   # custom
            visio.Settings.RasterExportCustomXResolution = 192
            visio.Settings.RasterExportCustomYResolution = 192
            visio.Settings.RasterExportColorFormat = 4   # 24-bit color
        except Exception as e:
            print(f"Export settings fallback: {e}")
        page.Export(PNG_PATH)
        print(f"PNG exported: {PNG_PATH}")

        # === Save VSDX ===
        doc.SaveAs(VSDX_PATH)
        print(f"VSDX saved: {VSDX_PATH}")

    except Exception as e:
        print(f"ERROR: {e}")
        import traceback
        traceback.print_exc()
    finally:
        visio.Quit()
        pythoncom.CoUninitialize()
        print("Visio closed.")

if __name__ == "__main__":
    main()