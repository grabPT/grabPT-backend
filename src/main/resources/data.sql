insert into category (code, name)
values ('HEALTH', '헬스'),
       ('PILATES', '필라테스'),
       ('GOLF', '골프'),
       ('TENNIS', '테니스'),
       ('SWIMMING', '수영'),
       ('BOXING', '복싱'),
       ('BADMINTON', '배드민턴'),
       ('RUNNING', '러닝'),
       ('DANCE', '댄스'),
       ('PINGPONG', '탁구');

INSERT INTO terms (title, version, is_required, created_at)
VALUES ('개인정보 수집 및 이용 동의', 'v1.0', true, NOW()),
       ('이용약관 동의', 'v1.0', true, NOW()),
       ('위치기반 서비스 약관 동의', 'v1.0', true, NOW()),
       ('만 14세 이상 확인', 'v1.0', true, NOW());
