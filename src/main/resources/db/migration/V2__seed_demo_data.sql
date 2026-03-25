insert into skills (code, display_name) values
('INCIDENT_RESPONSE', 'Incident Response'),
('PLATFORM_OPS', 'Platform Operations'),
('SECURITY_TRIAGE', 'Security Triage');

insert into employees (full_name, team_name, max_weekly_hours, preferred_shift_type) values
('Ava Collins', 'Core Ops', 32, 'DAY'),
('Liam Patel', 'Core Ops', 40, 'NIGHT'),
('Maya Hassan', 'Platform', 36, 'DAY'),
('Jonas Berg', 'Security', 30, 'SWING'),
('Nora Kim', 'Platform', 34, 'DAY'),
('Ethan Silva', 'Response', 38, 'SWING');

insert into employee_skills (employee_id, skill_id, skill_level) values
(1, 1, 4), (1, 2, 3),
(2, 1, 5), (2, 2, 4),
(3, 2, 5), (3, 1, 3),
(4, 3, 5), (4, 1, 3),
(5, 2, 4), (5, 3, 2),
(6, 1, 4), (6, 3, 3);

insert into availability_windows (employee_id, day_of_week, start_time, end_time) values
(1, 'MONDAY', '08:00:00', '18:00:00'),
(1, 'TUESDAY', '08:00:00', '18:00:00'),
(1, 'WEDNESDAY', '08:00:00', '18:00:00'),
(1, 'THURSDAY', '08:00:00', '18:00:00'),
(2, 'MONDAY', '18:00:00', '23:00:00'),
(2, 'TUESDAY', '18:00:00', '23:00:00'),
(2, 'WEDNESDAY', '18:00:00', '23:00:00'),
(2, 'THURSDAY', '18:00:00', '23:00:00'),
(3, 'MONDAY', '07:00:00', '17:00:00'),
(3, 'TUESDAY', '07:00:00', '17:00:00'),
(3, 'FRIDAY', '07:00:00', '17:00:00'),
(4, 'MONDAY', '12:00:00', '22:00:00'),
(4, 'WEDNESDAY', '12:00:00', '22:00:00'),
(4, 'FRIDAY', '12:00:00', '22:00:00'),
(5, 'TUESDAY', '08:00:00', '18:00:00'),
(5, 'WEDNESDAY', '08:00:00', '18:00:00'),
(5, 'THURSDAY', '08:00:00', '18:00:00'),
(6, 'MONDAY', '10:00:00', '22:00:00'),
(6, 'THURSDAY', '10:00:00', '22:00:00'),
(6, 'FRIDAY', '10:00:00', '22:00:00');

insert into shifts (shift_date, start_time, end_time, shift_type, priority, location, required_skill_id, required_headcount, notes) values
('2026-03-30', '09:00:00', '17:00:00', 'DAY', 'HIGH', 'Remote', 1, 2, 'Weekday incident response coverage'),
('2026-03-30', '18:00:00', '23:00:00', 'NIGHT', 'CRITICAL', 'Remote', 1, 1, 'After-hours escalation window'),
('2026-03-31', '08:00:00', '16:00:00', 'DAY', 'HIGH', 'Oslo', 2, 2, 'Platform release support'),
('2026-04-01', '12:00:00', '20:00:00', 'SWING', 'MEDIUM', 'Remote', 3, 1, 'Security alert triage'),
('2026-04-02', '14:00:00', '22:00:00', 'SWING', 'HIGH', 'Remote', 1, 2, 'Peak traffic incident buffer'),
('2026-04-03', '09:00:00', '17:00:00', 'DAY', 'MEDIUM', 'Oslo', 2, 1, 'Maintenance oversight');

insert into shift_assignments (shift_id, employee_id, assigned_at) values
(1, 1, current_timestamp),
(2, 2, current_timestamp),
(3, 3, current_timestamp),
(4, 4, current_timestamp);
