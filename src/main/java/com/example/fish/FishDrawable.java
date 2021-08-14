package com.example.fish;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FishDrawable extends Drawable {
    private Path mpath;
    private Paint mpaint;

    //除鱼身外所有透明度
private final static int OTHER_ALPHA=110;
    //鱼身透明度
    private final static int BODY_ALPHE=160;
    //转弯更自然的重心
private PointF middlePoint;
    //鱼的主角度
    private float fishMainAngle =90;

    public final static float HEAD_RADUIS=50;


    //鱼身长度
    private final static float BODY_LENGTH =3.2f*HEAD_RADUIS;

    //-----鱼鳍---
    //寻找鱼鳍开始点的线长
    private final static float FIND_FISH_LENGTH =0.9f*HEAD_RADUIS;
    //鱼鳍的长度
    private final static float FINS_LENGTH =1.3f*HEAD_RADUIS;
    //-----鱼尾----
    //尾部大圆的半径（圆心就是身体底部的中心）
    private final  float BIG_CIRCLE_RADIUS =HEAD_RADUIS*0.7f;
    //尾部中圆的的半径
    private final  float MIDDLE_CIRCLE_RADIUS=BIG_CIRCLE_RADIUS*0.6f;
    //尾部小圆的的半径
    private final  float SMALL_CIRCLE_RADIUS=BIG_CIRCLE_RADIUS*0.4f;
    //寻找尾部中圆圆心的线长
    private final  float FIND_MIDDLE_CIRCLE_LENTG=BIG_CIRCLE_RADIUS+MIDDLE_CIRCLE_RADIUS;
    //寻找尾部小圆圆心的线长
    private final  float FIND_SMALL_CIRCLE_LENGTH=MIDDLE_CIRCLE_RADIUS*(0.4f+2.7f);
    //寻找大三角形底边中心点的线长
    private final  float FIND_TRIANGLE_LENGTH =MIDDLE_CIRCLE_RADIUS*2.7f;
    private float currentValue=0;
    private PointF headPoint;

    public FishDrawable(){
        init();
    }

    private void init() {
        mpaint= new Paint();   //路径
        mpath =new Path();   // 画笔
        mpaint.setStyle(Paint.Style.FILL);   //画笔类型，填充
        mpaint.setARGB(OTHER_ALPHA,244,92,71);  //设置颜色
        mpaint.setAntiAlias(true);  //抗锯齿
        mpaint.setDither(true);   //防抖

        middlePoint =new PointF(4.19f*HEAD_RADUIS,4.19f*HEAD_RADUIS);
        ValueAnimator valueAnimator =ValueAnimator.ofFloat(0,360);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                currentValue = (float) animator.getAnimatedValue();
                invalidateSelf();

            }
        });
        valueAnimator.start();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        float fishAngle = (float) (fishMainAngle+Math.sin(Math.toRadians(currentValue))*10);

        //绘制鱼头
        headPoint=calculatPoint(middlePoint,BODY_LENGTH/2,fishAngle);
        canvas.drawCircle(headPoint.x,headPoint.y,HEAD_RADUIS,mpaint);
        //绘制右鱼鳍
        PointF rightFisnPoint=calculatPoint(headPoint,FIND_FISH_LENGTH,fishAngle-110);
        makeFins(canvas,rightFisnPoint,fishAngle,true);
        //绘制左鱼鳍
        PointF leftFisnPoint=calculatPoint(headPoint,FIND_FISH_LENGTH,fishAngle+110);
        makeFins(canvas,leftFisnPoint,fishAngle,false);

        //身体底部的中心点
        PointF bodyBottomCenterPoint =calculatPoint(headPoint,BODY_LENGTH,fishAngle-180);
        //画节肢1
       PointF middleCircleCenterPoint= makeSegment(canvas,bodyBottomCenterPoint,BIG_CIRCLE_RADIUS,MIDDLE_CIRCLE_RADIUS,
                FIND_MIDDLE_CIRCLE_LENTG,fishAngle,true);

        //画节肢2

        makeSegment(canvas,middleCircleCenterPoint,MIDDLE_CIRCLE_RADIUS,SMALL_CIRCLE_RADIUS,
                FIND_SMALL_CIRCLE_LENGTH,fishAngle,false);

        //画尾巴
        makeTriangle(canvas,middleCircleCenterPoint,FIND_TRIANGLE_LENGTH,BIG_CIRCLE_RADIUS,fishAngle);
        makeTriangle(canvas,middleCircleCenterPoint,FIND_TRIANGLE_LENGTH-10,BIG_CIRCLE_RADIUS-20,fishAngle);

        makeBody(canvas,headPoint,bodyBottomCenterPoint,fishAngle);
    }

    private void makeBody(Canvas canvas,PointF headPoint,PointF bodyBottomCenterPoint,float fishAngle){
        //身体的四个点
        PointF topLeftPoint =calculatPoint(headPoint,HEAD_RADUIS,fishAngle+90);
        PointF topRightPoint =calculatPoint(headPoint,HEAD_RADUIS,fishAngle-90);
        PointF bottomLeftPoint =calculatPoint(bodyBottomCenterPoint,BIG_CIRCLE_RADIUS,fishAngle+90);
        PointF bottomRightPoint =calculatPoint(bodyBottomCenterPoint,BIG_CIRCLE_RADIUS,fishAngle-90);

        //二阶贝塞尔曲线的控制点，决定鱼的胖瘦
        PointF controlLeft =calculatPoint(headPoint,BODY_LENGTH*0.56f,fishAngle+130);
        PointF controlRight=calculatPoint(headPoint,BODY_LENGTH*0.56f,fishAngle-130);

        //画身体
        mpath.reset();
        mpath.moveTo(topLeftPoint.x,topLeftPoint.y);
        mpath.quadTo(controlLeft.x,controlLeft.y,bottomLeftPoint.x,bottomLeftPoint.y);
        mpath.lineTo(bottomRightPoint.x,bottomRightPoint.y);
        mpath.quadTo(controlRight.x,controlRight.y,topRightPoint.x,topRightPoint.y);
        mpaint.setAlpha(BODY_ALPHE);
        canvas.drawPath(mpath,mpaint);
    }
    private void makeTriangle(Canvas canvas, PointF startPoint, float findCenterLenth, float findEdgeLength, float fishAngle) {

        float triangleAngle = (float) (fishMainAngle+Math.sin(Math.toRadians(currentValue*3))*30);

        //三角形底边的中心点
        PointF centerPoint =calculatPoint(startPoint,findCenterLenth,triangleAngle-180);

        //三角形底边的两点
        PointF leftPoint =calculatPoint(centerPoint,findEdgeLength,triangleAngle+90);
        PointF rightPoint =calculatPoint(centerPoint,findEdgeLength,triangleAngle-90);

        //绘制三角形
        mpath.reset();
        mpath.moveTo(startPoint.x,startPoint.y);
        mpath.lineTo(leftPoint.x,leftPoint.y);
        mpath.lineTo(rightPoint.x,rightPoint.y);
        canvas.drawPath(mpath,mpaint);

    }

    private PointF makeSegment(Canvas canvas,PointF bottomCenterPoint,float bigRadius,
                             float smallRadius,float findSmallCircleLength,float fishAngle,
                             boolean hasBigCircle){

        float segmentAngle;
        if(hasBigCircle){
            segmentAngle=(float) (fishMainAngle+Math.cos(Math.toRadians(currentValue*2))*20);
        }else {
            segmentAngle=(float) (fishMainAngle+Math.sin(Math.toRadians(currentValue*3))*30);
        }
        //梯形上底的中心点（中等大的圆的圆心）
        PointF upperCenterPoint= calculatPoint(bottomCenterPoint,findSmallCircleLength,segmentAngle-180);
        //梯形的四个点
        PointF bottomLeftPoint =calculatPoint(bottomCenterPoint,bigRadius,segmentAngle+90);
        PointF bottomRightPoint =calculatPoint(bottomCenterPoint,bigRadius,segmentAngle-90);
        PointF upperLeftPoint =calculatPoint(upperCenterPoint,smallRadius,segmentAngle+90);
        PointF upperRightPoint =calculatPoint(upperCenterPoint,smallRadius,segmentAngle-90);
        if (hasBigCircle){
            //画大圆
            canvas.drawCircle(bottomCenterPoint.x,bottomCenterPoint.y,bigRadius,mpaint);
        }
        //画小圆
        canvas.drawCircle(upperCenterPoint.x,upperCenterPoint.y,smallRadius,mpaint);
        //画梯形
        mpath.reset();
        mpath.moveTo(bottomLeftPoint.x,bottomLeftPoint.y);
        mpath.lineTo(upperLeftPoint.x,upperLeftPoint.y);
        mpath.lineTo(upperRightPoint.x,upperRightPoint.y);
        mpath.lineTo(bottomRightPoint.x,bottomRightPoint.y);
        canvas.drawPath(mpath,mpaint);

        return upperCenterPoint;
    }

    private void makeFins(Canvas canvas, PointF startPoint, float fishAngle,boolean isright) {
        float controlAngle =115;
        PointF endPoint =calculatPoint(startPoint,FINS_LENGTH,fishAngle-180);

        PointF controlPoint=calculatPoint(startPoint,1.8f*FINS_LENGTH,
                isright?fishAngle-controlAngle:fishAngle+controlAngle);
        mpath.reset();
        mpath.moveTo(startPoint.x,startPoint.y);
        mpath.quadTo(controlPoint.x,controlPoint.y,endPoint.x,endPoint.y);
        canvas.drawPath(mpath,mpaint);
    }

    /**
     *
     * @param startPoint 起始点
     * @param lenth  两点的距离
     * @param angle 两点连线与x轴的夹角
     * @return
     */
    public static PointF calculatPoint(PointF startPoint ,float lenth,float angle){
        //cosα*c=
        float deltaX =(float)(Math.cos(Math.toRadians(angle))*lenth);
        float deltaY =(float)(Math.sin(Math.toRadians(angle-180))*lenth);
        return new PointF(startPoint.x+deltaX,startPoint.y+deltaY);
    }

    @Override
    public void setAlpha(int alpha) {
mpaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
mpaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicHeight() {
        return (int)(8.38f*HEAD_RADUIS);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int)(8.38f*HEAD_RADUIS);
    }

    public PointF getMiddlePoint(){
        return  middlePoint;
    }

    public PointF getHeadPoint() {
        return headPoint;
    }

    public void setFishMainAngle(float fishMainAngle) {
        this.fishMainAngle = fishMainAngle;
    }
}
