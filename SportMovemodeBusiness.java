package com.medex.mdap.re.sportmovemode.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.medex.mdap.base.security.UserInfo;
import com.medex.mdap.pub.exception.BusinessException;
import com.medex.mdap.pub.exception.ExceptionCode;
import com.medex.mdap.re.sportaction.pojo.SportActionVO;
import com.medex.mdap.re.sportaction.service.SportActionService;
import com.medex.mdap.re.sportactionrecord.pojo.SportActionrecordVO;
import com.medex.mdap.re.sportactionrecord.service.SportActionrecordService;
import com.medex.mdap.re.sportactionrecord.view.SportActionrecordView;
import com.medex.mdap.re.sportactionrecorddetail.pojo.SportActionrecordDetailVO;
import com.medex.mdap.re.sportactionrecorddetail.service.SportActionrecordDetailService;
import com.medex.mdap.re.sportactionrecorddetail.view.SportActionrecordDetailView;
import com.medex.mdap.re.sporteachgroup.pojo.SportEachgroupVO;
import com.medex.mdap.re.sporteachgroup.service.SportEachgroupService;
import com.medex.mdap.re.sporteachgroup.view.SportEachgroupView;
import com.medex.mdap.re.sportmovemode.pojo.SportMovemodeVO;
import com.medex.mdap.re.sportmovemode.service.SportMovemodeService;
import com.medex.mdap.re.sportmovemode.view.RetrunMoveMode;
import com.medex.mdap.re.sportmovemode.view.SportMovemodeView;
import com.medex.mdap.re.sportscheme.service.SportSchemeService;
import com.medex.mdap.util.JSONProcessUtil;
import com.medex.mdap.util.PrimaryKeyUtil;

@Component
public class SportMovemodeBusiness {
	
	@Autowired
	SportSchemeService sportSchemeService;
	
	@Autowired
	SportMovemodeService sportMovemodeService;

	@Autowired
	SportActionrecordService sportActionrecordService;
	
	@Autowired
	SportActionService sportActionService;
	
	@Autowired
	SportEachgroupService sportEachgroupService;
	
	@Autowired
	private PrimaryKeyUtil primaryKeyUtil;
	
	@Autowired
	SportActionrecordDetailService sportActionrecordDetailService;
	
	//查询列表     支持模糊
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public List<SportMovemodeView> selectSportMovemodeAll(String json, UserInfo userInfo) {
		// TODO Auto-generated method stub
		//定义返回值
		List<SportMovemodeView> listSportMovemodeView = new ArrayList<SportMovemodeView>();
		List<SportMovemodeView> movemodes = new ArrayList<SportMovemodeView>();
		//获取前端传值
		SportMovemodeView sportMovemodeView = JSONProcessUtil.parseJsonObject(json, SportMovemodeView.class);
		//将登陆人的基本信息赋值给对象
		sportMovemodeView.setPk_group(userInfo.pkGroup);
		sportMovemodeView.setPk_org(userInfo.pkOrg);
		sportMovemodeView.setPk_dept(userInfo.pkDept);
		List<SportMovemodeView> listView = sportMovemodeService.selectSportMovemodeAll(sportMovemodeView);
		//查询动作方式
		for(SportMovemodeView movemodeView : listView){
			listSportMovemodeView.clear();
			movemodes.clear();
			//根据动作方式查询动作记录表中动作N的动作详情      获得所有组数的主键
			List<SportActionrecordView> listSportActionrecord = sportActionrecordService.selectCordPkActionByModePk(movemodeView);
			for(SportActionrecordView sportActionrecordView : listSportActionrecord){
				//去动作明细表         根据动作N的主键找到所有动作的主键
				List<SportActionrecordDetailView> listPkAction = sportActionrecordDetailService.selectPkActionByActionrecordPk(sportActionrecordView);

				//根据所有动作的主键找到动作的名称
				for(SportActionrecordDetailView sportActionrecordDetailView : listPkAction){
					//根据主键查询动作
					SportActionVO sportActionVO = sportActionService.selectById(sportActionrecordDetailView.getRe_sport_action());
					sportActionrecordDetailView.setActionName(sportActionVO.getName());
				}
				sportActionrecordView.setDetailList(listPkAction);
			}
			movemodeView.setListSportActionrecord(listSportActionrecord);
		}
		return listView;
	}

	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public List<RetrunMoveMode> insertSportMovemodeAll(String json, UserInfo userInfo) {
		// TODO Auto-generated method stub
		//定义返回值
		List<RetrunMoveMode> list = new ArrayList<RetrunMoveMode>();
		RetrunMoveMode retrun = new RetrunMoveMode();
		List<SportActionrecordView> cordListRe = new ArrayList<SportActionrecordView>();
		List<SportEachgroupView> eachListRe = new ArrayList<SportEachgroupView>();
		List<SportActionrecordDetailView> detailListRe = new ArrayList<SportActionrecordDetailView>();
		//获得前端传值
		RetrunMoveMode retrunMoveMode = JSONProcessUtil.parseJsonObject(json, RetrunMoveMode.class);
		//新增动作方式主表
		SportMovemodeVO sportMovemodeVo = new SportMovemodeVO();
		SportMovemodeView sportMovemodeView = retrunMoveMode.getSportMovemodeView();
		BeanUtils.copyProperties(sportMovemodeView, sportMovemodeVo);
		sportMovemodeVo.setPk_dept(userInfo.getPkDept());
		if(null != sportMovemodeView.getPk_group() || !"".equals(sportMovemodeView.getPk_group())){
			sportMovemodeVo.setPk_group(userInfo.getPkGroup());
		}
		sportMovemodeVo.setPk_org(userInfo.getPkOrg());
		sportMovemodeVo.setCreator(userInfo.getPkPsndocName());
		sportMovemodeVo.setCreatetime(primaryKeyUtil.createTs());
		sportMovemodeVo.setDr(0);
		sportMovemodeService.insert(sportMovemodeVo);
		//返回值
		BeanUtils.copyProperties(sportMovemodeVo,sportMovemodeView);
		retrun.setSportMovemodeView(sportMovemodeView);
		//新增动作记录
		List<SportActionrecordView> cordList = retrunMoveMode.getCordList();
		//每次根据新增的值覆盖之前的值
		SportActionrecordVO sportActionrecordVO = new SportActionrecordVO();
		for(SportActionrecordView sportActionrecordView : cordList){
			eachListRe.clear();
			detailListRe.clear();
			BeanUtils.copyProperties(sportActionrecordView, sportActionrecordVO);
			sportActionrecordVO.setPk_sport_movemode(sportMovemodeVo.getPk_sport_movemode());
			sportActionrecordVO.setDr(0);
			sportActionrecordService.insert(sportActionrecordVO);
			//返回值
			BeanUtils.copyProperties(sportActionrecordVO,sportActionrecordView);
			
			//新增动作明细
			List<SportActionrecordDetailView> detailList = sportActionrecordView.getDetailList();
			//每次根据新增的值覆盖之前的值
			SportActionrecordDetailVO SportActionrecordDetailVO = new SportActionrecordDetailVO();
			for(SportActionrecordDetailView sportActionrecordDetailView : detailList){
				BeanUtils.copyProperties(sportActionrecordDetailView, SportActionrecordDetailVO);
				SportActionrecordDetailVO.setRe_sport_actionrecord(sportActionrecordView.getPk_sport_actionrecord());
				sportActionrecordDetailService.insert(SportActionrecordDetailVO);
				//返回值
				BeanUtils.copyProperties( SportActionrecordDetailVO,sportActionrecordDetailView);
				detailListRe.add(sportActionrecordDetailView);
				
			}
			//新增每组记录
			List<SportEachgroupView> eachList = sportActionrecordView.getEachList();
			//每次根据新增的值覆盖之前的值
			SportEachgroupVO sportEachgroupVO = new SportEachgroupVO();
			for(SportEachgroupView SportEachgroupView : eachList){
				BeanUtils.copyProperties(SportEachgroupView, sportEachgroupVO);
				sportEachgroupVO.setPk_sport_actionrecord(sportActionrecordView.getPk_sport_actionrecord());
				sportEachgroupService.insert(sportEachgroupVO);
				//返回值
				BeanUtils.copyProperties( sportEachgroupVO,SportEachgroupView);
				eachListRe.add(SportEachgroupView);
			}
			sportActionrecordView.setDetailList(detailListRe);
			sportActionrecordView.setEachList(eachListRe);
			cordListRe.add(sportActionrecordView);
		}
		retrun.setCordList(cordListRe);
		list.add(retrun);
		return list;
	}

	
	//修改
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public List<RetrunMoveMode> updateSportMovemodeAll(String json, UserInfo userInfo) {
		// TODO Auto-generated method stub
		//定义返回值
		List<RetrunMoveMode> list = new ArrayList<RetrunMoveMode>();
		RetrunMoveMode retrun = new RetrunMoveMode();
		List<SportActionrecordView> cordListRe = new ArrayList<SportActionrecordView>();
		List<SportEachgroupView> eachListRe = new ArrayList<SportEachgroupView>();
		List<SportActionrecordDetailView> detailListRe = new ArrayList<SportActionrecordDetailView>();
		//接收前端传参
		RetrunMoveMode retrunMoveMode = JSONProcessUtil.parseJsonObject(json, RetrunMoveMode.class);
		//根据运动方式主表查询动作方式记录表   动作明细表   组数表并且删除
		SportMovemodeView sportMovemodeView = retrunMoveMode.getSportMovemodeView();
		//查询运动方式主表
		SportMovemodeVO sportMovemodeVo = new SportMovemodeVO();
		BeanUtils.copyProperties(sportMovemodeView,sportMovemodeVo);
		SportMovemodeVO sportMovemodeVO = sportMovemodeService.selectById(sportMovemodeVo);
		//查询动作记录表中的数据
		List<SportActionrecordView> cordListRe1 = sportActionrecordService.selectActionCordByModePK(sportMovemodeVO);
		for(SportActionrecordView sportActionrecordView : cordListRe1){
			//根据动作记录的主键查询动作明细表
			detailListRe = sportActionrecordDetailService.selectPkActionByActionrecordPk(sportActionrecordView);
			//执行删除动作明细表
			for(SportActionrecordDetailView sportActionrecordDetailView : detailListRe){
				sportActionrecordDetailService.deleteById(sportActionrecordDetailView.getPk_sport_actionrecord_detail());
			}
			//根据动作记录的主键查询每组记录表
			eachListRe = sportEachgroupService.selectEachGroupBypkCord(sportActionrecordView);
			//执行删除询每组记录表
			for(SportEachgroupView SportEachgroupView : eachListRe){
				sportEachgroupService.deleteById(SportEachgroupView.getPk_sport_eachgroup());
			}
			//执行删除动作记录
			sportActionrecordService.deleteById(sportActionrecordView.getPk_sport_actionrecord());
		}
		
		//修改主表
		BeanUtils.copyProperties(sportMovemodeView, sportMovemodeVo);
		sportMovemodeVo.setPk_dept(userInfo.getPkDept());
		if(null != sportMovemodeView.getPk_group() || !"".equals(sportMovemodeView.getPk_group())){
			sportMovemodeVo.setPk_group(userInfo.getPkGroup());
		}
		sportMovemodeVo.setPk_org(userInfo.getPkOrg());
		sportMovemodeVo.setModifier(userInfo.getPkPsndocName());
		sportMovemodeVo.setModifiedtime(primaryKeyUtil.createTs());
		/*if(sportMovemodeVO.getEntablestate() != sportMovemodeView.getEntablestate()){
			//是否被引用
			int count = sportSchemeService.selectCountByPkMove(sportMovemodeVO);
			if(count != 0){
				throw new BusinessException(ExceptionCode.UPDATEDRUGDIC);
			}
		}*/
		sportMovemodeService.updateSelectiveById(sportMovemodeVo);
		//返回值
		BeanUtils.copyProperties(sportMovemodeVo,sportMovemodeView);
		retrun.setSportMovemodeView(sportMovemodeView);
		//新增动作记录
		List<SportActionrecordView> cordList = retrunMoveMode.getCordList();
		//每次根据新增的值覆盖之前的值
		SportActionrecordVO sportActionrecordVO = new SportActionrecordVO();
		for(SportActionrecordView sportActionrecordView : cordList){
			eachListRe.clear();
			detailListRe.clear();
			BeanUtils.copyProperties(sportActionrecordView, sportActionrecordVO);
			sportActionrecordVO.setPk_sport_movemode(sportMovemodeVo.getPk_sport_movemode());
			sportActionrecordService.insert(sportActionrecordVO);
			//返回值
			BeanUtils.copyProperties(sportActionrecordVO,sportActionrecordView);
			
			//新增动作明细
			List<SportActionrecordDetailView> detailList = sportActionrecordView.getDetailList();
			//每次根据新增的值覆盖之前的值
			SportActionrecordDetailVO SportActionrecordDetailVO = new SportActionrecordDetailVO();
			for(SportActionrecordDetailView sportActionrecordDetailView : detailList){
				BeanUtils.copyProperties(sportActionrecordDetailView, SportActionrecordDetailVO);
				SportActionrecordDetailVO.setRe_sport_actionrecord(sportActionrecordView.getPk_sport_actionrecord());
				sportActionrecordDetailService.insert(SportActionrecordDetailVO);
				//返回值
				BeanUtils.copyProperties( SportActionrecordDetailVO,sportActionrecordDetailView);
				detailListRe.add(sportActionrecordDetailView);
				
			}
			//新增每组记录
			List<SportEachgroupView> eachList = sportActionrecordView.getEachList();
			//每次根据新增的值覆盖之前的值
			SportEachgroupVO sportEachgroupVO = new SportEachgroupVO();
			for(SportEachgroupView SportEachgroupView : eachList){
				BeanUtils.copyProperties(SportEachgroupView, sportEachgroupVO);
				sportEachgroupVO.setPk_sport_actionrecord(sportActionrecordView.getPk_sport_actionrecord());
				sportEachgroupService.insert(sportEachgroupVO);
				//返回值
				BeanUtils.copyProperties( sportEachgroupVO,SportEachgroupView);
				eachListRe.add(SportEachgroupView);
			}
			sportActionrecordView.setDetailList(detailListRe);
			sportActionrecordView.setEachList(eachListRe);
			cordListRe.add(sportActionrecordView);
		}
		retrun.setCordList(cordListRe);
		list.add(retrun);
		return list;
	}

	//删除
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public List<RetrunMoveMode> deleteSportMovemodeAll(String json) {
		//定义返回值
		List<RetrunMoveMode> list = new ArrayList<RetrunMoveMode>();
		//全局变量
		List<SportActionrecordView> cordListRe = new ArrayList<SportActionrecordView>();
		List<SportEachgroupView> eachListRe = new ArrayList<SportEachgroupView>();
		List<SportActionrecordDetailView> detailListRe = new ArrayList<SportActionrecordDetailView>();
		
		//查询要修改的这条对象的详情 
		
		//获得前端传值
		RetrunMoveMode retrunMoveMode = JSONProcessUtil.parseJsonObject(json, RetrunMoveMode.class);
		//查询运动方式主表
		SportMovemodeView sportMovemodeView = retrunMoveMode.getSportMovemodeView();
		SportMovemodeVO sportMovemodeVo = new SportMovemodeVO();
		BeanUtils.copyProperties(sportMovemodeView,sportMovemodeVo);
		/*//是否被引用
		int count = sportSchemeService.selectCountByPkMove(sportMovemodeVo);
		if(count != 0){
			throw new BusinessException(ExceptionCode.DELETEDRUGDIC);
		}*/
		SportMovemodeVO sportMovemodeVO = sportMovemodeService.selectById(sportMovemodeVo);
		//查询动作记录表中的数据
		cordListRe = sportActionrecordService.selectActionCordByModePK(sportMovemodeVO);
		for(SportActionrecordView sportActionrecordView : cordListRe){
			//根据动作记录的主键查询动作明细表
			detailListRe = sportActionrecordDetailService.selectPkActionByActionrecordPk(sportActionrecordView);
			//执行删除动作明细表
			for(SportActionrecordDetailView sportActionrecordDetailView : detailListRe){
				sportActionrecordDetailService.deleteById(sportActionrecordDetailView.getPk_sport_actionrecord_detail());
			}
			//根据动作记录的主键查询每组记录表
			eachListRe = sportEachgroupService.selectEachGroupBypkCord(sportActionrecordView);
			//执行删除询每组记录表
			for(SportEachgroupView SportEachgroupView : eachListRe){
				sportEachgroupService.deleteById(SportEachgroupView.getPk_sport_eachgroup());
			}
			//执行删除动作记录
			sportActionrecordService.deleteById(sportActionrecordView.getPk_sport_actionrecord());
		}
		sportMovemodeService.deleteById(sportMovemodeVO.getPk_sport_movemode());
		list.add(retrunMoveMode);
		return list;
	}

	//单个查询
	public List<RetrunMoveMode> selectSportMovemodeByPKMode(String json) {
		// TODO Auto-generated method stub
		//定义返回值
		List<RetrunMoveMode> list = new ArrayList<RetrunMoveMode>();
		RetrunMoveMode retrun = new RetrunMoveMode();
		List<SportActionrecordView> cordListRe = new ArrayList<SportActionrecordView>();
		//获得前端传值
		SportMovemodeView sportMovemodeView = JSONProcessUtil.parseJsonObject(json, SportMovemodeView.class);
		//查询运动方式主表
		SportMovemodeVO sportMovemodeVo = new SportMovemodeVO();
		BeanUtils.copyProperties(sportMovemodeView,sportMovemodeVo);
		SportMovemodeVO sportMovemodeVO = sportMovemodeService.selectById(sportMovemodeVo);
		//查询动作记录表中的数据
		cordListRe = sportActionrecordService.selectActionCordByModePK(sportMovemodeVO);
		
		for(SportActionrecordView sportActionrecordView : cordListRe){
			//根据动作记录的主键查询动作明细表
			List<SportActionrecordDetailView> detailListRe = sportActionrecordDetailService.selectPkActionByActionrecordPk(sportActionrecordView);
			for(SportActionrecordDetailView sportActionrecordDetailView : detailListRe){
				//根据主键查询动作
				SportActionVO sportActionVO = sportActionService.selectById(sportActionrecordDetailView.getRe_sport_action());
				sportActionrecordDetailView.setActionName(sportActionVO.getName());
			}
			
			
			//根据动作记录的主键查询每组记录表
			List<SportEachgroupView> eachListRe = sportEachgroupService.selectEachGroupBypkCord(sportActionrecordView);
			sportActionrecordView.setEachList(eachListRe);
			sportActionrecordView.setDetailList(detailListRe);
		}
		BeanUtils.copyProperties(sportMovemodeVO,sportMovemodeView);
		retrun.setSportMovemodeView(sportMovemodeView);
		retrun.setCordList(cordListRe);
		list.add(retrun);
		return list;
	}

	public List<SportMovemodeView> selectSportMovemode(String json, UserInfo userInfo) {
		//定义返回值
		List<SportMovemodeView> listSportMovemodeView = new ArrayList<SportMovemodeView>();
		List<SportMovemodeView> movemodes = new ArrayList<SportMovemodeView>();
		//获取前端传值
		SportMovemodeView sportMovemodeView = JSONProcessUtil.parseJsonObject(json, SportMovemodeView.class);
		//将登陆人的基本信息赋值给对象
		sportMovemodeView.setPk_group(userInfo.pkGroup);
		sportMovemodeView.setPk_org(userInfo.pkOrg);
		sportMovemodeView.setPk_dept(userInfo.pkDept);
		List<SportMovemodeView> listView = sportMovemodeService.selectSportMovemode(sportMovemodeView);
		//查询动作方式
		for(SportMovemodeView movemodeView : listView){
			listSportMovemodeView.clear();
			movemodes.clear();
			//根据动作方式查询动作记录表中动作N的动作详情      获得所有组数的主键
			List<SportActionrecordView> listSportActionrecord = sportActionrecordService.selectCordPkActionByModePk(movemodeView);
			for(SportActionrecordView sportActionrecordView : listSportActionrecord){
				//去动作明细表         根据动作N的主键找到所有动作的主键
				List<SportActionrecordDetailView> listPkAction = sportActionrecordDetailService.selectPkActionByActionrecordPk(sportActionrecordView);

				//根据所有动作的主键找到动作的名称
				for(SportActionrecordDetailView sportActionrecordDetailView : listPkAction){
					//根据主键查询动作
					SportActionVO sportActionVO = sportActionService.selectById(sportActionrecordDetailView.getRe_sport_action());
					sportActionrecordDetailView.setActionName(sportActionVO.getName());
				}
				sportActionrecordView.setDetailList(listPkAction);
			}
			movemodeView.setListSportActionrecord(listSportActionrecord);
		}
		return listView;
	}
}
