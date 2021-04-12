REM $Header: XXST_Migrate_lov.sql_exec 001.000.000 2021-03-18 RPATHANIA $
declare
begin
hxc_lov_migration.migrate_lov_region(
p_region_code => 'XXST_STERIA_SPC'
, p_region_app_short_name => 'HXC'
, p_force => 'Y'
);

commit;

hxc_lov_migration.migrate_lov_region(
p_region_code => 'XXST_STERIA_AA_TYPE'
, p_region_app_short_name => 'HXC'
, p_force => 'Y'
);

commit;

hxc_lov_migration.migrate_lov_region(
p_region_code => 'XXST_STERIA_ACTIVITY_TYPE'
, p_region_app_short_name => 'HXC'
, p_force => 'Y'
);

commit;


hxc_lov_migration.migrate_lov_region(
p_region_code => 'XXST_STERIA_WORK_TYPE'
, p_region_app_short_name => 'HXC'
, p_force => 'Y'
);

commit;


hxc_lov_migration.migrate_lov_region(
p_region_code => 'HXC_CUI_TASK_LOV'
, p_region_app_short_name => 'HXC'
, p_force => 'Y'
);

commit;



hxc_lov_migration.migrate_lov_region(
p_region_code => 'HXC_CUI_PROJECT_LOV'
, p_region_app_short_name => 'HXC'
, p_force => 'Y'
);

commit;

exception
when others then
dbms_output.put_line(sqlerrm );
end;
