/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 5.6.22-log : Database - yttps_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`yttps_db` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `yttps_db`;

/*Table structure for table `customer` */

DROP TABLE IF EXISTS `customer`;

CREATE TABLE `customer` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `customer_name` text NOT NULL COMMENT '客户名称（一般为企业名称）',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Data for the table `customer` */

insert  into `customer`(`id`,`customer_name`,`create_time`,`update_time`) values (1,'eservice','2018-08-21 11:26:37','2018-08-21 13:31:18'),(2,'汉堃科技','2018-08-21 13:31:37',NULL);

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Data for the table `user` */

insert  into `user`(`id`,`account`,`name`,`password`) values (1,'admin','HT','admin');

/*Table structure for table `visitor_info` */

DROP TABLE IF EXISTS `visitor_info`;

CREATE TABLE `visitor_info` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(100) NOT NULL COMMENT '姓名',
  `IdCard` varchar(18) NOT NULL COMMENT '身份证',
  `company` varchar(100) NOT NULL COMMENT '公司名称',
  `dateTime` date NOT NULL COMMENT '日期',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '是否来过，0、没来，1、来过',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

/*Data for the table `visitor_info` */

insert  into `visitor_info`(`id`,`name`,`IdCard`,`company`,`dateTime`,`status`) values (1,'身份证必须判断','430121199101885231','姓名为空要判断','2019-03-14',0),(2,'是','43022420000322421X','广州网迅信息技术有限公司','2019-03-14',0),(3,'公司为空要判断','430421200001200371','提示信息为：某行数据不要完整','2019-03-14',0),(4,'刘六','431023199950609621','是','2019-03-14',0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
