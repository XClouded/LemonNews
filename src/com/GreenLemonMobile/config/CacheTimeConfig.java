package com.GreenLemonMobile.config;

public class CacheTimeConfig {

	public static final long MINUTE = 1000 * 60;
	public static final long HOUR = MINUTE * 60;
	public static final long DAY = HOUR * 24;

	public static final long DEFAULT = -1;// Ĭ��

	public static final long CRAZY_BUY = MINUTE * 5;// �������
	public static final long JD_NEWS = MINUTE * 5;// �����챨
	public static final long RECOMMEND = MINUTE * 30;// ����ϲ��
	public static final long CATEGORY = HOUR;// �����б�
	public static final long PROVINCE = DAY * 3;// ������
	public static final long IMAGE = DAY * 3;// �κ�ͼƬ
	public static final long HOME_ACTIVTIES = MINUTE*5;//���

}
