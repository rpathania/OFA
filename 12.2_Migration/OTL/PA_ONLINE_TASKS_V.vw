REM $Header: PA_ONLINE_TASKS_V.vw 001.000.000 2021-03-18 RPATHANIA $

  CREATE OR REPLACE FORCE EDITIONABLE VIEW "APPS"."PA_ONLINE_TASKS_V" ("PROJECT_ID", "PROJECT_NUMBER", "TASK_ID", "TASK_NUMBER", "TASK_NAME", "TASK_DESCRIPTION", "START_DATE", "COMPLETION_DATE", "CHARGEABLE_FLAG", "BILLABLE_FLAG", "ORG_ID", "TASK_DETAILS") AS 
  SELECT t.project_id,
          p.segment1 project_number,
          t.task_id,
          t.task_number,
          (SELECT pt.task_name
             FROM pa_tasks pt
            WHERE pt.task_id = t.top_task_id)
             task_name,
          (SELECT pt.description
             FROM pa_tasks pt
            WHERE pt.task_id = t.top_task_id)
             description,
          t.start_date,
          t.completion_date,
          t.chargeable_flag,
          t.billable_flag,
          imp.org_id,
          t.task_number || '-' || t.task_name task_details
     FROM pa_tasks t,
          pa_projects_all p,
          pa_implementations imp,
          pa_lookups lu
    WHERE     lu.lookup_type = 'PA_TASKS_TO_DISPLAY'
          AND lu.lookup_code =
                 NVL (fnd_profile.VALUE ('PA_TASKS_DISPLAYED'), 'ALL')
          AND (   (lu.lookup_code = 'CHARGEABLE' AND t.chargeable_flag = 'Y')
               OR (lu.lookup_code = 'ALL')
               OR (    lu.lookup_code = 'LOWEST'
                   AND pa_task_utils.check_child_exists (t.task_id) = 0))
          AND (    p.project_id = t.project_id
               AND (imp.org_id = p.org_id OR t.allow_cross_charge_flag = 'Y'))
          AND t.task_number LIKE '%.1'
          AND EXISTS
                 (SELECT 'Y'
                    FROM pa_tasks pt1
                   WHERE pt1.wbs_level = 2 AND pt1.project_id = t.project_id)
   UNION ALL
   SELECT project_id,
          project_number,
          task_id,
          task_number,
          task_name,
          task_description,
          start_date,
          completion_date,
          chargeable_flag,
          billable_flag,
          org_id,
          task_details
     FROM xxst_ec_sp_projects_mv2;
